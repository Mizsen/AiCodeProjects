import sqlite3
import time
import os
import json
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
import undetected_chromedriver as uc
from fake_useragent import UserAgent

import random


# boss_spider.py
# Boss 直聘岗位信息抓取脚本

# 配置路径
current_dir = os.path.dirname(os.path.abspath(__file__))
STATE_FILE = "login_data.json"

chrome_binary_path = os.path.join(current_dir, "chrome114-win64", "chrome.exe")
chromedriver_path = os.path.join(current_dir, "chrome114-win64", "chromedriver.exe")


# 初始化浏览器
def init_browser():

    # 设置 User-Agent
    ua = UserAgent(platforms=["pc"])
    user_agent = ua.random
    print("🎲 使用的 User-Agent：", user_agent)
    chrome_options = Options()
    chrome_options.binary_location = chrome_binary_path
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--start-maximized")
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")
    chrome_options.add_argument(f'--user-agent={user_agent}')  # 使用随机 User-Agent


    # service = Service(executable_path=chromedriver_path)
    # driver = webdriver.Chrome(service=service, options=chrome_options)

    # 初始化 undetected-chromedriver
    driver = uc.Chrome(
        driver_executable_path=chromedriver_path,  # chromedriver 路径
        options=chrome_options,
        version_main=114  # 根据你本地 Chrome 的版本号填写，例如 125
    )

    # 进一步隐藏自动化特征
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": """
        delete navigator.__proto__.webdriver;
        window.chrome = {runtime: {}};
        Object.defineProperty(navigator, 'languages', {
          get: () => ['en-US', 'en']
        });
        """
    })

    return driver


class BossJobScraper:
    def __init__(self, url):
        self.driver = init_browser()
        self.url = url
        self.conn = sqlite3.connect("../jobs.db")
        self.create_table()
        self.load_login_state()

    def load_login_state(self):
        """加载登录状态（Cookies + localStorage + sessionStorage）"""
        if not os.path.exists(STATE_FILE):
            print("⚠️ 未找到登录状态文件，请先运行 login_boss.py 登录 Boss 直聘！")
            exit()

        print("🍪 正在加载登录状态...")
        with open(STATE_FILE, "r", encoding="utf-8") as f:
            data = json.load(f)

        self.driver.get("https://www.zhipin.com/")

        # 添加 Cookies
        for cookie in data.get("cookies", []):
            try:
                cookie.pop("sameSite", None)
                cookie.pop("expiry", None)
                cookie.pop("httpOnly", None)
                cookie.pop("secure", None)
                self.driver.add_cookie(cookie)
            except Exception as e:
                print("⚠️ 添加 Cookie 出错：", e)

        # 设置 localStorage 和 sessionStorage
        self.set_storage(data.get("localStorage", {}), "localStorage")
        self.set_storage(data.get("sessionStorage", {}), "sessionStorage")

        # 刷新页面以恢复登录状态
        self.driver.get(self.url)
        print("✅ 登录状态加载完成")

    def set_storage(self, storage_dict, storage_type="localStorage"):
        script = ""
        for key, value in storage_dict.items():
            script += f"window.{storage_type}.setItem('{key}', '{value}');"
        if script:
            self.driver.execute_script(script)

    def create_table(self):
        """创建数据库表"""
        self.conn.execute(
            """CREATE TABLE IF NOT EXISTS jobs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            
            title TEXT,
            company TEXT,
            location TEXT,
            salary TEXT,
            experience TEXT,
            education TEXT,
            description TEXT,
            url TEXT UNIQUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )"""
        )
        self.conn.commit()

    def scroll_to_bottom(self, max_scrolls=20, scroll_pause=5):
        """滚动加载岗位列表"""
        print("⬇️ 开始滚动加载岗位...")
        last_height = self.driver.execute_script("return document.body.scrollHeight")
        scroll_count = 0

        while scroll_count < max_scrolls:
            self.driver.execute_script(
                "window.scrollTo(0, document.body.scrollHeight);"
            )
            print(f"🔄 第 {scroll_count + 1} 次滚动")
            time.sleep(scroll_pause)

            new_height = self.driver.execute_script("return document.body.scrollHeight")
            if new_height == last_height:
                print("🔚 页面已到底，停止滚动")
                break

            last_height = new_height
            scroll_count += 1

        print("✅ 滚动加载完成")




    def scroll_to_bottom(self, scroll_step=300, max_retries=50, retry_interval=2):
        print("⬇️ 开始智能滚动加载岗位...")

        job_selector = "ul.rec-job-list .job-card-box a.job-name"
        seen_jobs = set()
        retry = 0

        while retry < max_retries:
            # 获取当前岗位链接
            job_elements = self.driver.find_elements(By.CSS_SELECTOR, job_selector)
            current_job_hrefs = [job.get_attribute('href') for job in job_elements if job.get_attribute('href')]

            new_jobs = [href for href in current_job_hrefs if href not in seen_jobs]

            if new_jobs:
                print(f"🆕 新加载 {len(new_jobs)} 个岗位")
                seen_jobs.update(current_job_hrefs)
                retry = 0
            else:
                print("🔄 未检测到新岗位，继续滚动")
                retry += 1

            # 滚动页面
            scroll_step = random.randint(200, 500)  # 随机步长
            self.driver.execute_script(f"window.scrollBy(0, {scroll_step});")

            # 随机等待时间
            time.sleep(random.uniform(1, 3))

            # 检查是否到底
            current_position = self.driver.execute_script("return window.scrollY + window.innerHeight;")
            total_height = self.driver.execute_script("return document.body.scrollHeight;")
            if current_position >= total_height:
                print("🔚 已滚动至页面底部")
                break

        print("✅ 滚动加载完成，共加载岗位数量：", len(seen_jobs))
        return list(seen_jobs)

    def extract_jobs(self):
        """提取岗位列表信息"""
        WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "ul.rec-job-list"))
        )

        job_items = self.driver.find_elements(
            By.CSS_SELECTOR, "ul.rec-job-list li.job-card-box"
        )
        jobs = []

        for item in job_items:
            try:
                a_tag = item.find_element(By.CSS_SELECTOR, "a.job-name")
                href = a_tag.get_attribute("href")

                # 检查是否已经是完整的 URL
                if href.startswith("http"):
                    job_url = href
                else:
                    job_url = "https://www.zhipin.com" + href  # 补全 URL

                job_name = a_tag.text.strip()

                salary = item.find_element(
                    By.CSS_SELECTOR, "span.job-salary"
                ).text.strip()
                company_name = item.find_element(
                    By.CSS_SELECTOR, "span.boss-name"
                ).text.strip()
                location = item.find_element(
                    By.CSS_SELECTOR, "span.company-location"
                ).text.strip()

                # 提取所有 tag 标签
                tag_elements = item.find_elements(By.CSS_SELECTOR, "ul.tag-list li")
                tags = [tag.text.strip() for tag in tag_elements]

                # 提取前两个作为经验 & 学历
                experience = tags[0] if len(tags) > 0 else None
                education = tags[1] if len(tags) > 1 else None

                # 剩下的作为技能标签
                skills = tags[2:] if len(tags) > 2 else []

                jobs.append(
                    {
                        "title": job_name,
                        "company": company_name,
                        "location": location,
                        "salary": salary,
                        "experience": experience,
                        "education": education,
                        "url": job_url,
                    }
                )
            except Exception as e:
                print("⚠️ 提取某条数据出错：", e)
                continue

        return jobs

    def extract_job_description(self, job_element):
        """
        点击岗位元素，等待右侧职位描述区域加载，提取内容
        :param job_element: 岗位的 WebElement 元素
        :return: 完整的职位描述（含标签、Boss信息、工作地址）
        """
        try:
            # 点击岗位卡片
            job_element.click()
            print("🖱️ 已点击岗位卡片，等待职位描述加载...")

            # 等待职位描述区域加载完成
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "div.job-detail-body p.desc"))
            )

            # 提取职位描述文本
            description = self.driver.find_element(By.CSS_SELECTOR, "div.job-detail-body p.desc").text.strip()

            # 提取岗位标签
            label_elements = self.driver.find_elements(By.CSS_SELECTOR, "ul.job-label-list li")
            labels = [label.text.strip() for label in label_elements]
            labels_str = "，".join(labels) if labels else "无"

            # 提取 Boss 信息
            boss_name = self.driver.find_element(By.CSS_SELECTOR, "div.job-boss-info h2.name").text.strip()
            company_and_title = self.driver.find_element(By.CSS_SELECTOR, "div.boss-info-attr").text.strip()

            try:
                # 提取工作地址
                job_address = self.driver.find_element(By.CSS_SELECTOR, "p.job-address-desc").text.strip()
            except Exception as e:
                print("⚠️ 提取工作地址失败：", e)
                job_address = "无"

            # 构造完整描述
            full_description = (
                f"{description}\n\n"
                f"——————\n"
                f"岗位标签：{labels_str}\n"
                f"招聘人：{boss_name}\n"
                f"公司：{company_and_title}\n"
                f"工作地址：{job_address}"
            )

            return full_description

        except Exception as e:
            print("⚠️ 提取职位描述失败：", e)
            return None
    


    def save_single_job(self, job):
        """保存单个岗位信息（含描述）"""
        cursor = self.conn.cursor()
        cursor.execute(
            """
            INSERT OR IGNORE INTO jobs (title, company, location, salary, experience, education, description, url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
            (
                job["title"],
                job["company"],
                job["location"],
                job["salary"],
                job["experience"],
                job["education"],
                job.get("description", ""),
                job["url"],
            ),
        )
        self.conn.commit()

    def scrape_jobs(self, max_scrolls=20):
        """主抓取流程"""
        print("🌐 正在加载岗位页面...")
        self.driver.get(self.url)
        time.sleep(5)

        seen_urls = set()
        total_jobs = 0

        # self.scroll_to_bottom(max_scrolls=max_scrolls)
        self.scroll_to_bottom(scroll_step=300, max_retries=50, retry_interval=2)

        jobs = self.extract_jobs()
        if not jobs:
            print("⚠️ 没有岗位信息")
            return

        for job in jobs:
            if job["url"] in seen_urls:
                continue

            # 找到对应的岗位元素
            try:
                # 重新查找岗位元素（避免 StaleElementReferenceException）
                job_element = self.driver.find_element(
                    By.XPATH,
                    f"//a[@href='{job['url'].replace('https://www.zhipin.com', '')}']/ancestor::li",
                )
            except Exception as e:
                print("⚠️ 无法找到岗位元素，跳过：", e)
                continue

            description = self.extract_job_description(job_element)
            job["description"] = description

            self.save_single_job(job)
            seen_urls.add(job["url"])
            total_jobs += 1

        print(f"✅ 总共保存 {total_jobs} 条岗位信息（含职位描述）")

    def close(self):
        self.driver.quit()
        self.conn.close()


# =======================
# ✅ 示例运行
# =======================
if __name__ == "__main__":
    target_url = "https://www.zhipin.com/web/geek/job?query=Java&city=101200100"  # 可替换为任意岗位页
    scraper = BossJobScraper(target_url)
    try:
        scraper.scrape_jobs(max_scrolls=5)
    finally:
        scraper.close()

        print("👋 浏览器已关闭，数据库连接已关闭")

import sys
import os
import json
import time
import random
import sqlite3
import pandas as pd
import datetime

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

import undetected_chromedriver as uc
from fake_useragent import UserAgent


class FontParser:
    def __init__(self):
        """初始化字体解析器及字符映射关系"""
        self.mapping = {
            "\ue031": "0",
            "\ue032": "1",
            "\ue033": "2",
            "\ue034": "3",
            "\ue035": "4",
            "\ue036": "5",
            "\ue037": "6",
            "\ue038": "7",
            "\ue039": "8",
            "\ue03a": "9"
        }
    
    def update_mapping(self, new_mapping):
        """更新映射关系"""
        self.mapping.update(new_mapping)
        return f"已更新映射关系，当前映射数: {len(self.mapping)}"
    
    def parse_text(self, text):
        """
        解析文本并返回结果
        :param text: 待解析的文本
        :return: 包含原始文本、解析结果和分析信息的字典
        """
        if not text:
            return {"original_text": "", "parsed_result": "", "analysis": []}
        
        # 计算解析结果
        parsed_result = ''.join([self.mapping.get(char, char) for char in text])
        
        # 生成详细分析信息
        analysis = []
        for char in text:
            code = f"\\u{ord(char):x}"
            mapped_value = self.mapping.get(char, "未映射")
            analysis.append({
                "char": char,
                "unicode": code,
                "mapped_value": mapped_value
            })
        
        # 返回结果字典
        result = {
            "original_text": text,
            "parsed_result": parsed_result,
            "analysis": analysis
        }
        
        # 打印解析结果（方便查看）
        # self._print_result(result)
        
        return result
    
    def _print_result(self, result):
        """内部方法：打印结果格式化打印"""
        print("=" * 50)
        print(f"原始字符: {result['original_text']}")
        print(f"解析结果: {result['parsed_result']}\n")
        
        print("字符分析详情:")
        for idx, info in enumerate(result['analysis']):
            print(f"  字符 {idx + 1}: {info['char']}")
            print(f"    Unicode编码: {info['unicode']}")
            print(f"    映射值: {info['mapped_value']}")
        print("=" * 50 + "\n")


class BossJobScraper:
    # 类变量：路径配置
    CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
    # STATE_FILE = "login_data.json"  # 开发
    STATE_FILE = "login_data.json"  # 生产

    CHROME_BINARY_PATH = os.path.join(CURRENT_DIR, "chrome114-win64", "chrome.exe")
    CHROMEDRIVER_PATH = os.path.join(CURRENT_DIR, "chrome114-win64", "chromedriver.exe")

    def __init__(self, url, query_type, city):
        super().__init__()
        self.url = url
        self.query_type = query_type
        self.city = city
        self.parser = FontParser()

    def init_browser(self):
        """初始化浏览器"""
        ua = UserAgent(platforms=["pc"])
        user_agent = ua.random
        print("🎲 使用的 User-Agent：", user_agent)

        chrome_options = Options()
        chrome_options.binary_location = self.CHROME_BINARY_PATH
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--start-maximized")
        chrome_options.add_argument("--disable-blink-features=AutomationControlled")
        chrome_options.add_argument(f"--user-agent={user_agent}")

        # 使用 undetected-chromedriver
        driver = uc.Chrome(
            driver_executable_path=self.CHROMEDRIVER_PATH,
            options=chrome_options,
            version_main=114,  # 根据你的 Chrome 版本修改
        )

        # 隐藏自动化特征
        driver.execute_cdp_cmd(
            "Page.addScriptToEvaluateOnNewDocument",
            {
                "source": """
            delete navigator.__proto__.webdriver;
            window.chrome = {runtime: {}};
            Object.defineProperty(navigator, 'languages', {
              get: () => ['en-US', 'en']
            });
            """
            },
        )

        return driver

    def load_login_state(self):
        """加载登录状态（Cookies + localStorage + sessionStorage）"""
        if not os.path.exists(self.STATE_FILE):
            print("⚠️ 未找到登录状态文件，请先运行 login_boss.py 登录 Boss 直聘！")
            raise Exception("登录状态文件缺失")

        print("🍪 正在加载登录状态...")
        with open(self.STATE_FILE, "r", encoding="utf-8") as f:
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
            query_type TEXT,
            city TEXT,
            channel TEXT,
            created_at TEXT
        )"""
        )
        self.conn.commit()

    def scroll_to_bottom(self, scroll_step=300, max_retries=50, retry_interval=2):
        print("⬇️ 开始智能滚动加载岗位...")

        job_selector = "ul.rec-job-list .job-card-box a.job-name"
        seen_jobs = set()
        retry = 0

        while retry < max_retries:
            print("🔄 正在滚动加载岗位...", max_retries - retry)
            job_elements = self.driver.find_elements(By.CSS_SELECTOR, job_selector)
            current_job_hrefs = [
                j.get_attribute("href") for j in job_elements if j.get_attribute("href")
            ]
            new_jobs = [h for h in current_job_hrefs if h not in seen_jobs]

            if new_jobs:
                seen_jobs.update(current_job_hrefs)
                retry = 0
            else:
                retry += 1

            scroll_step = random.randint(200, 500)
            self.driver.execute_script(f"window.scrollBy(0, {scroll_step});")
            time.sleep(random.uniform(0.5, 1.5))

            pos = self.driver.execute_script(
                "return window.scrollY + window.innerHeight;"
            )
            total = self.driver.execute_script("return document.body.scrollHeight;")
            if pos >= total:
                break

        print("✅ 滚动加载完成，共加载岗位数量：", len(seen_jobs))
        return list(seen_jobs)

    def extract_jobs(self):
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
                job_url = (
                    href if href.startswith("http") else "https://www.zhipin.com" + href
                )
                job_name = a_tag.text.strip()
                salary = item.find_element(
                    By.CSS_SELECTOR, "span.job-salary"
                ).text.strip()
                company = item.find_element(
                    By.CSS_SELECTOR, "span.boss-name"
                ).text.strip()
                location = item.find_element(
                    By.CSS_SELECTOR, "span.company-location"
                ).text.strip()
                tags = [
                    t.text.strip()
                    for t in item.find_elements(By.CSS_SELECTOR, "ul.tag-list li")
                ]
                experience = tags[0] if len(tags) > 0 else ""
                education = tags[1] if len(tags) > 1 else ""

             

                jobs.append(
                    {
                        "title": job_name,
                        "company": company,
                        "location": location,
                        "salary": self.parser.parse_text(salary)['parsed_result'],
                        "experience": experience,
                        "education": education,
                        "description": "",
                        "url": job_url,
                        "query_type": self.query_type,
                        "channel": "Boss直聘",
                        "created_at": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        "city": self.city,
                    }
                )
            except Exception as e:
                print("⚠️ 提取失败：", e)
                continue
        return jobs

    def extract_job_description(self, job_element):
        try:
            job_element.click()
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(
                    (By.CSS_SELECTOR, "div.job-detail-body p.desc")
                )
            )
            description = self.driver.find_element(
                By.CSS_SELECTOR, "div.job-detail-body p.desc"
            ).text.strip()
            labels = [
                e.text.strip()
                for e in self.driver.find_elements(
                    By.CSS_SELECTOR, "ul.job-label-list li"
                )
            ]
            boss_name = self.driver.find_element(
                By.CSS_SELECTOR, "div.job-boss-info h2.name"
            ).text.strip()
            company_and_title = self.driver.find_element(
                By.CSS_SELECTOR, "div.boss-info-attr"
            ).text.strip()
            try:
                job_address = self.driver.find_element(
                    By.CSS_SELECTOR, "p.job-address-desc"
                ).text.strip()
            except:
                job_address = "无"
            full_description = f"{description}\n\n岗位标签：{', '.join(labels)}\n招聘人：{boss_name}\n公司：{company_and_title}\n工作地址：{job_address}"
            return full_description
        except Exception as e:
            print("⚠️ 提取职位描述失败：", e)
            return ""

    def save_single_job(self, job):
        cursor = self.conn.cursor()

        # now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        cursor.execute(
            """INSERT OR IGNORE INTO jobs 
            (title, company, location, salary, experience, education, description, url, query_type, city, channel, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (
                job["title"],
                job["company"],
                job["location"],
                job["salary"],
                job["experience"],
                job["education"],
                job["description"],
                job["url"],
                job["query_type"],
                job["city"],
                job["channel"],
                job["created_at"]
            ),
        )
        self.conn.commit()

    def scrape_jobs(self):

        print("📝 准备数据库...")
        self.conn = sqlite3.connect("T:/MyApp/zhaopin-win-amd64/jobs.db")  # 生产
        self.create_table()
        print("✅ 数据库准备完成")

        try:
            print("🌐 正在初始化浏览器...")
            self.driver = self.init_browser()
            self.load_login_state()
        except Exception as e:
            print("⚠️ 初始化浏览器时发生错误：", e)
            exit(1)

        """抓取岗位信息"""
        try:
            print("🔍 开始爬取岗位信息...")
            self.driver.maximize_window()
            print(self.url)
            print("🌐 正在加载岗位页面...")

            self.driver.get(self.url)
            time.sleep(5)

            self.scroll_to_bottom(scroll_step=300, max_retries=50, retry_interval=2)

            jobs = self.extract_jobs()
            total = 0
            for job in jobs:

                try:
                    job_element = self.driver.find_element(
                        By.XPATH,
                        f"//a[@href='{job['url'].replace('https://www.zhipin.com', '')}']/ancestor::li",
                    )
                    description = self.extract_job_description(job_element)
                    job["description"] = description
                    self.save_single_job(job)
                    total += 1

                except Exception as e:
                    print("⚠️ 处理岗位失败：", e)
                    continue

                time.sleep(random.randint(1, 3))

            print(f"✅ 已保存 {total} 条岗位信息（含职位描述）")

            print("✅ 数据爬取完成")

        except Exception as e:
            print("⚠️ 爬取数据时发生错误：", e)
            raise e

    def close(self):
        """关闭浏览器和数据库连接"""
        self.driver.quit()
        self.conn.close()


def main():
    """主函数"""
    # 硬编码的URL列表和查询类型
    url = "https://www.zhipin.com/web/geek/jobs?city={}&query={}"
    # query_types = ["java", "python", "算法", "前端"]
    query_types = ["服务员"]

    # city_map={"北京":"101010100","上海":"101020100","深圳":"101280600","广州":"101280100","武汉":"101200100"}
    city_map = {"上海": "101020100"}
    scrapers = []

    # 动态创建多个BossJobScraper对象
    for city_name, city_code in city_map.items():
        for query_type in query_types:
            scraper_url = url.format(city_code, query_type)
            scraper = BossJobScraper(
                url=scraper_url, query_type=query_type, city=city_name
            )
            scrapers.append(scraper)

    print(f"🚀 启动Boss直聘爬虫，共 {len(scrapers)} 个任务...")

    # 依次执行每个爬虫任务
    for i, scraper in enumerate(scrapers):
        print(f"\n🔄 开始处理第 {i+1}/{len(scrapers)} 个任务...")
        try:
            scraper.scrape_jobs()
            time.sleep(random.randint(5,10) * 60)
        except Exception as e:
            print(f"❌ 第 {i+1} 个任务出错：{str(e)}")
        finally:
            scraper.close()

    print("✅ 所有爬虫任务完成，浏览器已关闭")


if __name__ == "__main__":
    main()

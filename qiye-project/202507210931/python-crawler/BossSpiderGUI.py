import tkinter as tk
from tkinter import messagebox
import sqlite3
import time
import os
import json
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

import random


# ========================
# 登录管理类（简化）
# ========================
class LoginManager:
    def __init__(self, url="https://www.zhipin.com/"):
        self.url = url
        self.STATE_FILE = "login_data.json"
        current_dir = os.path.dirname(os.path.abspath(__file__))
        chrome_binary_path = os.path.join(current_dir, "chrome114-win64", "chrome.exe")
        chromedriver_path = os.path.join(current_dir, "chrome114-win64", "chromedriver.exe")

        chrome_options = Options()
        chrome_options.binary_location = chrome_binary_path
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--start-maximized")

        service = Service(executable_path=chromedriver_path)
        self.driver = webdriver.Chrome(service=service, options=chrome_options)

    def has_login_state(self):
        if not os.path.exists(self.STATE_FILE):
            return False
        try:
            with open(self.STATE_FILE, 'r', encoding='utf-8') as f:
                data = json.load(f)
            return isinstance(data, dict) and 'cookies' in data and 'localStorage' in data
        except Exception:
            return False

    def save_login_data(self):
        cookies = self.driver.get_cookies()
        localStorage = self.driver.execute_script("return window.localStorage;")
        sessionStorage = self.driver.execute_script("return window.sessionStorage;")

        data = {
            "cookies": cookies,
            "localStorage": dict(localStorage),
            "sessionStorage": dict(sessionStorage)
        }

        with open(self.STATE_FILE, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=4)
        print("✅ 登录信息已保存")

    def load_login_data(self):
        with open(self.STATE_FILE, 'r', encoding='utf-8') as f:
            data = json.load(f)
        for cookie in data["cookies"]:
            cookie.pop('sameSite', None)
            cookie.pop('expiry', None)
            cookie.pop('httpOnly', None)
            cookie.pop('secure', None)
            try:
                self.driver.add_cookie(cookie)
            except Exception as e:
                print("⚠️ 添加 Cookie 出错：", e)
        self.driver.get(self.url)

    def login_flow(self):
        self.driver.get(self.url)
        time.sleep(3)
        if self.has_login_state():
            self.load_login_data()
        else:
            messagebox.showinfo("提示", "请手动登录 Boss 直聘账户，完成后点击确认")
            self.save_login_data()

    def close(self):
        self.save_login_data()
        self.driver.quit()


# ========================
# 爬虫类（BossJobScraper）
# ========================
class BossJobScraper:
    def __init__(self, url, query_type, city):
        self.url = url
        self.query_type = query_type
        self.city = city
        self.conn = sqlite3.connect("../jobs.db")
        self.create_table()
        self.driver = self.init_browser()

    def init_browser(self):
        current_dir = os.path.dirname(os.path.abspath(__file__))
        chrome_binary_path = os.path.join(current_dir, "chrome114-win64", "chrome.exe")
        chromedriver_path = os.path.join(current_dir, "chrome114-win64", "chromedriver.exe")

        chrome_options = Options()
        chrome_options.binary_location = chrome_binary_path
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--start-maximized")

        service = Service(executable_path=chromedriver_path)
        driver = webdriver.Chrome(service=service, options=chrome_options)
        return driver

    def create_table(self):
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
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )"""
        )
        self.conn.commit()

    def scroll_to_bottom(self):
        retry = 0
        seen_jobs = set()
        while retry < 50:
            job_elements = self.driver.find_elements(By.CSS_SELECTOR, "ul.rec-job-list .job-card-box a.job-name")
            current_hrefs = [j.get_attribute("href") for j in job_elements if j.get_attribute("href")]
            new_jobs = [h for h in current_hrefs if h not in seen_jobs]
            if new_jobs:
                seen_jobs.update(current_hrefs)
                retry = 0
            else:
                retry += 1
            scroll_step = random.randint(200, 500)
            self.driver.execute_script(f"window.scrollBy(0, {scroll_step});")
            time.sleep(random.uniform(1, 3))
            pos = self.driver.execute_script("return window.scrollY + window.innerHeight;")
            total = self.driver.execute_script("return document.body.scrollHeight;")
            if pos >= total:
                break

    def extract_jobs(self):
        WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "ul.rec-job-list"))
        )
        job_items = self.driver.find_elements(By.CSS_SELECTOR, "ul.rec-job-list li.job-card-box")
        jobs = []
        for item in job_items:
            try:
                a_tag = item.find_element(By.CSS_SELECTOR, "a.job-name")
                href = a_tag.get_attribute("href")
                job_url = href if href.startswith("http") else "https://www.zhipin.com" + href
                job_name = a_tag.text.strip()
                salary = item.find_element(By.CSS_SELECTOR, "span.job-salary").text.strip()
                company = item.find_element(By.CSS_SELECTOR, "span.boss-name").text.strip()
                location = item.find_element(By.CSS_SELECTOR, "span.company-location").text.strip()
                tags = [t.text.strip() for t in item.find_elements(By.CSS_SELECTOR, "ul.tag-list li")]
                experience = tags[0] if len(tags) > 0 else ""
                education = tags[1] if len(tags) > 1 else ""

                jobs.append({
                    "title": job_name,
                    "company": company,
                    "location": location,
                    "salary": salary,
                    "experience": experience,
                    "education": education,
                    "description": "",
                    "url": job_url,
                    "query_type": self.query_type,
                    "city": self.city
                })
            except Exception as e:
                print("⚠️ 提取失败：", e)
                continue
        return jobs

    def save_single_job(self, job):
        cursor = self.conn.cursor()
        cursor.execute(
            """INSERT OR IGNORE INTO jobs 
            (title, company, location, salary, experience, education, description, url, query_type, city)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (job["title"], job["company"], job["location"], job["salary"],
             job["experience"], job["education"], job["description"],
             job["url"], job["query_type"], job["city"])
        )
        self.conn.commit()

    def scrape_jobs(self):
        self.driver.get(self.url)
        time.sleep(5)
        self.scroll_to_bottom()
        jobs = self.extract_jobs()
        for job in jobs:
            self.save_single_job(job)
        print(f"✅ 已保存 {len(jobs)} 条岗位信息")
        self.conn.close()
        self.driver.quit()


# ========================
# GUI 界面类
# ========================
class BossScraperGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Boss 直聘爬虫工具")

        tk.Label(root, text="岗位类型（如：Java、货运司机）：").pack(pady=5)
        self.query_entry = tk.Entry(root, width=50)
        self.query_entry.pack(pady=5)

        tk.Label(root, text="城市（如：北京、武穴）：").pack(pady=5)
        self.city_entry = tk.Entry(root, width=50)
        self.city_entry.pack(pady=5)

        self.login_button = tk.Button(root, text="登录 Boss 账号", command=self.login)
        self.login_button.pack(pady=10)

        self.scrape_button = tk.Button(root, text="开始爬取数据", command=self.scrape, state=tk.DISABLED)
        self.scrape_button.pack(pady=10)

        self.login_manager = None

    def login(self):
        self.login_manager = LoginManager()
        self.login_manager.login_flow()
        messagebox.showinfo("提示", "登录成功！")
        self.scrape_button.config(state=tk.NORMAL)

    def scrape(self):
        query = self.query_entry.get().strip()
        city = self.city_entry.get().strip()
        if not query or not city:
            messagebox.showwarning("警告", "请输入岗位类型和城市")
            return

        url = f"https://www.zhipin.com/web/geek/job?query={query}&city={city}"
        scraper = BossJobScraper(url, query, city)
        scraper.scrape_jobs()
        messagebox.showinfo("完成", "数据爬取完成！")


# ========================
# 启动 GUI
# ========================
if __name__ == "__main__":
    root = tk.Tk()
    app = BossScraperGUI(root)
    root.mainloop()
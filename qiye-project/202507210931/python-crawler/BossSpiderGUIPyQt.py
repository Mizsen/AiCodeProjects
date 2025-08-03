import sys
import os
import json
import time
import random
import sqlite3
import pandas as pd
import datetime


from PyQt5.QtWidgets import (
    QApplication, QWidget, QLabel, QLineEdit, QPushButton, QVBoxLayout,
    QTableView, QTextEdit, QHBoxLayout, QMessageBox, QFileDialog, QDialog
)
from PyQt5.QtCore import Qt, QAbstractTableModel, QVariant
from PyQt5.QtSql import QSqlTableModel, QSqlDatabase
from PyQt5.QtGui import QTextCursor

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

import undetected_chromedriver as uc
from fake_useragent import UserAgent


from PyQt5.QtCore import QObject, pyqtSignal, QThread
from PyQt5.QtCore import QTimer
from PyQt5.QtWidgets import QTextEdit
from PyQt5.QtGui import QTextCursor
from PyQt5.QtCore import QObject
from PyQt5.QtCore import QObject, pyqtSignal


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


class LoginSignalEmitter(QObject):
    request_login_prompt = pyqtSignal(str)  # 请求弹出登录提示
    login_complete = pyqtSignal()          # 登录完成
    login_error = pyqtSignal(str)          # 登录出错

class LogSignalEmitter(QObject):
    log_message = pyqtSignal(str)

class ScrapeSignalEmitter(QObject):
    request_login_prompt = pyqtSignal(str)  # 请求登录提示
    show_error = pyqtSignal(str)            # 显示错误
    show_info = pyqtSignal(str)             # 显示提示信息
    scrape_complete = pyqtSignal()          # 爬虫完成

# ========================
# 登录管理类（简化）
# ========================
class LoginManager(QObject):


    # 类变量：路径配置
    CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
    # STATE_FILE = "login_data.json"  # 开发
    STATE_FILE = "login_data.json"  # 生产

    CHROME_BINARY_PATH = os.path.join(CURRENT_DIR, "chrome114-win64", "chrome.exe")
    CHROMEDRIVER_PATH = os.path.join(CURRENT_DIR, "chrome114-win64", "chromedriver.exe")

    def __init__(self, url="https://www.zhipin.com/", signal_emitter=None):
        super().__init__()
        self.signal_emitter = signal_emitter
        self.url = url
        self.driver = self.init_browser()

    def init_browser(self):
        chrome_options = Options()
        chrome_options.binary_location = self.CHROME_BINARY_PATH
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--start-maximized")

        service = Service(executable_path=self.CHROMEDRIVER_PATH)
        driver = webdriver.Chrome(service=service, options=chrome_options)
        return driver

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
            print("🍪 已加载登录状态")
        else:
            # 发出信号，请求主线程弹出提示
            if self.signal_emitter:
                self.signal_emitter.request_login_prompt.emit("请手动登录 Boss 直聘账户，完成后点击确认")
            else:
                # 用于调试或非线程调用
                reply = QMessageBox.information(None, "提示", "请手动登录 Boss 直聘账户，完成后点击确认", QMessageBox.Ok)
                if reply == QMessageBox.Ok:
                    self.save_login_data()

    def close(self):
        self.save_login_data()
        self.driver.quit()
        print("🔒 已保存登录状态并关闭浏览器")


# ========================
# 爬虫类（BossJobScraper）
# ========================
class BossJobScraper(QObject):
    # 类变量：路径配置
    CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
    # STATE_FILE = "login_data.json"  # 开发
    STATE_FILE = "login_data.json"  # 生产

    CHROME_BINARY_PATH = os.path.join(CURRENT_DIR, "chrome114-win64", "chrome.exe")
    CHROMEDRIVER_PATH = os.path.join(CURRENT_DIR, "chrome114-win64", "chromedriver.exe")

    def __init__(self, url, query_type, city, signal_emitter=None):
        super().__init__()
        self.url = url
        self.query_type = query_type
        self.city = city
        self.signal_emitter = signal_emitter
        # self.conn = sqlite3.connect("../jobs.db")  #开发
        self.conn = sqlite3.connect("jobs.db")  #生产
        self.parser = FontParser()

        self.driver = self.init_browser()
        self.create_table()
        self.load_login_state()

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
        chrome_options.add_argument(f'--user-agent={user_agent}')

        # 使用 undetected-chromedriver
        driver = uc.Chrome(
            driver_executable_path=self.CHROMEDRIVER_PATH,
            options=chrome_options,
            version_main=114  # 根据你的 Chrome 版本修改
        )

        # 隐藏自动化特征
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
    
    

    def load_login_state(self):
        """加载登录状态（Cookies + localStorage + sessionStorage）"""
        if not os.path.exists(self.STATE_FILE):
            if self.signal_emitter:
                self.signal_emitter.show_error.emit("未找到登录状态文件，请先登录 Boss 直聘！")
            else:
                QMessageBox.critical(None, "错误", "未找到登录状态文件，请先登录 Boss 直聘！")
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
            current_job_hrefs = [j.get_attribute("href") for j in job_elements if j.get_attribute("href")]
            new_jobs = [h for h in current_job_hrefs if h not in seen_jobs]

            if new_jobs:
                seen_jobs.update(current_job_hrefs)
                retry = 0
            else:
                retry += 1

            scroll_step = random.randint(200, 500)
            self.driver.execute_script(f"window.scrollBy(0, {scroll_step});")
            time.sleep(random.uniform(0.5, 1.5))

            pos = self.driver.execute_script("return window.scrollY + window.innerHeight;")
            total = self.driver.execute_script("return document.body.scrollHeight;")
            if pos >= total:
                break

        print("✅ 滚动加载完成，共加载岗位数量：", len(seen_jobs))
        return list(seen_jobs)

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
                    "salary": self.parser.parse_text(salary)['parsed_result'],
                    "experience": experience,
                    "education": education,
                    "description": "",
                    "url": job_url,
                    "query_type": self.query_type,
                    "channel":"Boss直聘",
                    "city": self.city
                })
            except Exception as e:
                print("⚠️ 提取失败：", e)
                continue
        return jobs

    def extract_job_description(self, job_element):
        try:
            job_element.click()
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "div.job-detail-body p.desc"))
            )
            description = self.driver.find_element(By.CSS_SELECTOR, "div.job-detail-body p.desc").text.strip()
            labels = [e.text.strip() for e in self.driver.find_elements(By.CSS_SELECTOR, "ul.job-label-list li")]
            boss_name = self.driver.find_element(By.CSS_SELECTOR, "div.job-boss-info h2.name").text.strip()
            company_and_title = self.driver.find_element(By.CSS_SELECTOR, "div.boss-info-attr").text.strip()
            try:
                job_address = self.driver.find_element(By.CSS_SELECTOR, "p.job-address-desc").text.strip()
            except:
                job_address = "无"
            full_description = f"{description}\n\n岗位标签：{', '.join(labels)}\n招聘人：{boss_name}\n公司：{company_and_title}\n工作地址：{job_address}"
            return full_description
        except Exception as e:
            print("⚠️ 提取职位描述失败：", e)
            return ""

    def save_single_job(self, job):
        cursor = self.conn.cursor()
        
        now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        cursor.execute(
            """INSERT OR IGNORE INTO jobs 
            (title, company, location, salary, experience, education, description, url, query_type, city, channel,created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)""",
            (job["title"], job["company"], job["location"], job["salary"],
             job["experience"], job["education"], job["description"],
             job["url"], job["query_type"], job["city"],
             job["channel"], now)
        )
        self.conn.commit()

    def scrape_jobs(self):

        try:
            print("🔍 开始爬取岗位信息...")
            self.driver.maximize_window()

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
                        f"//a[@href='{job['url'].replace('https://www.zhipin.com', '')}']/ancestor::li"
                    )
                    description = self.extract_job_description(job_element)
                    job["description"] = description
                    self.save_single_job(job)
                    total += 1
                except Exception as e:
                    print("⚠️ 处理岗位失败：", e)
                    continue
            print(f"✅ 已保存 {total} 条岗位信息（含职位描述）")
            
            

            if self.signal_emitter:
                self.signal_emitter.scrape_complete.emit()
            else:
                print("✅ 数据爬取完成")

        except Exception as e:
            if self.signal_emitter:
                self.signal_emitter.show_error.emit(f"爬虫出错：{str(e)}")
            else:
                raise e
        finally:
            self.close()
            print("🔒 数据库连接已关闭")

    def close(self):
        """关闭浏览器和数据库连接"""
        self.driver.quit()
        self.conn.close()

# ========================
# 数据库查看类（使用 QTableView）
# ========================
class DatabaseViewer(QDialog):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("查看数据库数据")
        self.resize(800, 400)

        self.model = QSqlTableModel(db=self.connect_db())
        self.model.setTable("jobs")
        self.model.select()

        self.table_view = QTableView()
        self.table_view.setModel(self.model)

        layout = QVBoxLayout()
        layout.addWidget(self.table_view)
        self.setLayout(layout)

    def connect_db(self):
        db = QSqlDatabase.addDatabase('QSQLITE')
        # db.setDatabaseName("../jobs.db")# 开发
        db.setDatabaseName("jobs.db")  # 生产
        
        if not db.open():
            QMessageBox.critical(self, "错误", "无法连接数据库")
        return db



class Worker(QObject):
    finished = pyqtSignal()
    error = pyqtSignal(str)

    def __init__(self, func):
        super().__init__()
        self.func = func

    def run(self):
        try:
            self.func()
        except Exception as e:
            self.error.emit(str(e))
        self.finished.emit()



# ========================
# 日志输出重定向类（用于 PyQt5）
# ========================      
class LoggerOutput:
    def __init__(self, signal_emitter):
        self.signal_emitter = signal_emitter

    def write(self, message):
        if message.strip():
            self.signal_emitter.log_message.emit(message.strip())

    def flush(self):
        pass


# ========================
# GUI 主界面类（PyQt5）
# ========================
class BossScraperGUI(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Boss 直聘爬虫工具")
        self.resize(600, 500)

        self.log_text = QTextEdit()
        self.log_text.setReadOnly(True)

        # 重定向 print 输出到日志框
        sys.stdout = LoggerOutput(self.log_text)

        self.query_label = QLabel("岗位类型（如：java、货运司机）：")
        self.query_input = QLineEdit("java")
        self.city_label = QLabel("城市（如：北京）：")
        self.city_input = QLineEdit("深圳")
        self.url_label = QLabel("请输入 Boss 职位搜索完整 URL")
        self.url_input = QLineEdit("https://www.zhipin.com/web/geek/jobs?city=101280600&query=java")

        self.login_button = QPushButton("登录 Boss 账号")
        self.scrape_button = QPushButton("开始爬取数据")
        self.view_button = QPushButton("查看数据库数据")
        self.export_button = QPushButton("导出 CSV")

        # self.scrape_button.setEnabled(False)

        layout = QVBoxLayout()
        layout.addWidget(self.query_label)
        layout.addWidget(self.query_input)
        layout.addWidget(self.city_label)
        layout.addWidget(self.city_input)
        layout.addWidget(self.url_label)
        layout.addWidget(self.url_input)

        btn_layout = QHBoxLayout()
        btn_layout.addWidget(self.login_button)
        btn_layout.addWidget(self.scrape_button)
        btn_layout.addWidget(self.view_button)
        btn_layout.addWidget(self.export_button)

        layout.addLayout(btn_layout)
        layout.addWidget(QLabel("日志输出："))
        layout.addWidget(self.log_text)

        self.setLayout(layout)

        self.login_manager = None

        self.login_button.clicked.connect(self.handle_login)
        self.scrape_button.clicked.connect(self.handle_scrape)
        self.view_button.clicked.connect(self.handle_view)
        self.export_button.clicked.connect(self.handle_export)

        self.log_signal_emitter = LogSignalEmitter()
        sys.stdout = LoggerOutput(self.log_signal_emitter)
        # 将日志信号连接到 QTextEdit 更新
        self.log_signal_emitter.log_message.connect(self.append_log)

        
    def append_log(self, message):
        self.log_text.append(message.strip())  # 使用 append 自动换行
        

    # ========================
    # 登录处理函数
    def handle_login(self):
        self.login_button.setEnabled(False)
       
        self.login_signal_emitter = LoginSignalEmitter()
        self.thread = QThread()
        self.login_manager = LoginManager(signal_emitter=self.login_signal_emitter)
        self.login_manager.moveToThread(self.thread)

        # Worker 用于执行登录任务
        self.worker = Worker(self._login_task)
        self.worker.moveToThread(self.thread)

        # 线程启动
        self.thread.started.connect(self.worker.run)
        self.worker.finished.connect(self.thread.quit)
        self.worker.finished.connect(self.worker.deleteLater)
        self.thread.finished.connect(self.thread.deleteLater)
        self.worker.error.connect(self.on_worker_error)

        # 连接登录提示信号
        self.login_signal_emitter.request_login_prompt.connect(self.show_login_prompt)
        self.login_signal_emitter.login_complete.connect(self.on_login_complete)
        self.login_signal_emitter.login_error.connect(self.on_worker_error)
     
        self.thread.start()
        
    def on_worker_error(self, message):
        QMessageBox.critical(self, "错误", message)

    def show_login_prompt(self, message):
        reply = QMessageBox.information(self, "提示", message, QMessageBox.Ok)
        if reply == QMessageBox.Ok:
            self.login_manager.save_login_data()
            self.login_manager.driver.quit()
            self.login_signal_emitter.login_complete.emit()


    def on_login_complete(self):
        QMessageBox.information(self, "提示", "登录成功！")
        self.scrape_button.setEnabled(True)
        self.login_button.setEnabled(True)  
        self.login_manager.close()  # 关闭浏览器   


    def _login_task(self):
        try:
            self.login_manager.login_flow()
            # 使用信号触发主线程操作
            self.login_signal_emitter.login_complete.emit()
        except Exception as e:
            self.login_signal_emitter.login_error.emit(str(e))


    # ========================
    # 爬虫处理函数
    def handle_scrape(self):
        query = self.query_input.text().strip()
        city = self.city_input.text().strip()
        if not query or not city:
            QMessageBox.warning(self, "警告", "请输入岗位类型和城市")
            return

        url = self.url_input.text().strip()
        self.scrape_button.setEnabled(False)

        # 创建信号发射器
        self.scrape_signal_emitter = ScrapeSignalEmitter()
        self.scrape_thread = QThread()
        self.scrape_worker = Worker(lambda: self._scrape_task(url, query, city))
        self.scrape_worker.moveToThread(self.scrape_thread)

        # 线程启动
        self.scrape_thread.started.connect(self.scrape_worker.run)
        self.scrape_worker.finished.connect(self.scrape_thread.quit)
        self.scrape_worker.finished.connect(self.scrape_worker.deleteLater)
        self.scrape_thread.finished.connect(self.scrape_thread.deleteLater)
        self.scrape_worker.error.connect(self.on_worker_error)

        # 连接信号
        self.scrape_signal_emitter.show_error.connect(self.show_scrape_error)
        self.scrape_signal_emitter.show_info.connect(self.show_scrape_info)
        self.scrape_signal_emitter.scrape_complete.connect(self.on_scrape_complete)

        self.scrape_thread.start()

    def _scrape_task(self, url, query, city):
        scraper = BossJobScraper(url, query, city, signal_emitter=self.scrape_signal_emitter)
        scraper.scrape_jobs()

    def show_scrape_error(self, message):
        QMessageBox.critical(self, "错误", message)

    def show_scrape_info(self, message):
        QMessageBox.information(self, "提示", message)

    def on_scrape_complete(self):
        QMessageBox.information(self, "完成", "数据爬取完成！")
        self.scrape_button.setEnabled(True)


    def handle_view(self):
        viewer = DatabaseViewer()
        viewer.show()
        viewer.exec_()

    def handle_export(self):
        file_path, _ = QFileDialog.getSaveFileName(self, "保存 CSV 文件", "", "CSV 文件 (*.csv)")
        if file_path:
            # conn = sqlite3.connect("../jobs.db")# 开发
            conn = sqlite3.connect("jobs.db")  # 生产
            df = pd.read_sql_query("SELECT * FROM jobs", conn)
            df.to_csv(file_path, index=False, encoding="utf-8-sig")
            conn.close()
            QMessageBox.information(self, "完成", f"数据已导出到 {file_path}")


# ========================
# 启动 GUI
# ========================
if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = BossScraperGUI()
    window.show()
    sys.exit(app.exec_())
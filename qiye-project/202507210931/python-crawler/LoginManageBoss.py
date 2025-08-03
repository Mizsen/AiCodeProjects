from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
import os
import json
import time

class LoginManager:
    def __init__(self, url="https://www.zhipin.com/"):
        self.url = url
        self.STATE_FILE = "login_data.json"

        # 获取当前脚本所在目录
        current_dir = os.path.dirname(os.path.abspath(__file__))
        # 构建 chrome.exe 和 chromedriver.exe 的相对路径
        chrome_binary_path = os.path.join(current_dir, "chrome114-win64", "chrome.exe")
        chromedriver_path = os.path.join(current_dir, "chrome114-win64", "chromedriver.exe")

        # 创建 ChromeOptions
        chrome_options = Options()
        chrome_options.binary_location = chrome_binary_path
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--start-maximized")

        # 创建服务
        service = Service(executable_path=chromedriver_path)

        # 初始化浏览器
        self.driver = webdriver.Chrome(service=service, options=chrome_options)

    def has_login_state(self):
        """判断登录状态文件是否存在且内容合法"""
        if not os.path.exists(self.STATE_FILE):
            return False
        try:
            with open(self.STATE_FILE, 'r', encoding='utf-8') as f:
                data = json.load(f)
            if isinstance(data, dict) and 'cookies' in data and 'localStorage' in data:
                return True
            return False
        except (json.JSONDecodeError, UnicodeDecodeError, FileNotFoundError):
            return False

    def save_login_data(self):
        """保存当前登录状态数据到文件"""
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
        """从文件加载登录状态数据并恢复"""
        with open(self.STATE_FILE, 'r', encoding='utf-8') as f:
            data = json.load(f)

        # 设置 cookies
        for cookie in data["cookies"]:
            cookie.pop('sameSite', None)
            cookie.pop('expiry', None)
            cookie.pop('httpOnly', None)
            cookie.pop('secure', None)
            try:
                self.driver.add_cookie(cookie)
            except Exception as e:
                print("⚠️ 添加 Cookie 出错：", e)

        # 设置 localStorage 和 sessionStorage
        def set_storage(storage_dict, storage_type="localStorage"):
            script = ""
            for key, value in storage_dict.items():
                script += f"window.{storage_type}.setItem('{key}', '{value}');"
            if script:
                self.driver.execute_script(script)

        set_storage(data["localStorage"], "localStorage")
        set_storage(data["sessionStorage"], "sessionStorage")

        # 刷新页面以应用登录状态
        self.driver.get(self.url)
        print("✅ 登录状态已恢复")

    def login_flow(self):
        """主流程：加载状态或手动登录"""
        self.driver.get(self.url)
        time.sleep(3)

        if self.has_login_state():
            try:
                self.load_login_data()
            except Exception as e:
                print("⚠️ 加载登录状态失败，可能文件损坏或已过期：", e)
                print("请手动重新登录...")
                input("登录完成后按回车继续...")
                self.save_login_data()
        else:
            print("请手动登录账户，登录完成后按回车键继续...")
            input()  # 等待用户登录
            self.save_login_data()

    def close(self):
        """退出前保存最新状态并关闭浏览器"""
        print("🔄 正在退出前再次保存最新的登录状态...")
        self.save_login_data()
        self.driver.quit()
        print("👋 浏览器已关闭")


# ========================
# 使用示例
# ========================
if __name__ == "__main__":
    manager = LoginManager()
    manager.login_flow()
    input("按回车键退出...")
    manager.close()




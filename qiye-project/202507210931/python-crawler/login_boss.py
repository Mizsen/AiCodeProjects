from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
import os
import json
import time

# 状态文件路径
STATE_FILE = "T:/MyApp/zhaopin-win-amd64/login_data.json"

# 获取当前脚本所在目录
current_dir = os.path.dirname(os.path.abspath(__file__))

# 构建 chrome.exe 和 chromedriver.exe 的相对路径
chrome_binary_path = os.path.join(current_dir, "chrome114-win64", "chrome.exe")
chromedriver_path = os.path.join(current_dir, "chrome114-win64", "chromedriver.exe")

# 创建 ChromeOptions 并指定浏览器位置
chrome_options = Options()
chrome_options.binary_location = chrome_binary_path

# 添加常用选项（可选）
chrome_options.add_argument("--disable-gpu")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument("--start-maximized")

# 创建服务
service = Service(executable_path=chromedriver_path)

# 初始化浏览器
driver = webdriver.Chrome(service=service, options=chrome_options)

# 打开目标网址
driver.get("https://www.zhipin.com/")

time.sleep(3)  # 等待页面加载

# 判断是否有登录状态数据
def has_login_state():
    if not os.path.exists(STATE_FILE):
        return False
    try:
        with open(STATE_FILE, 'r', encoding='utf-8') as f:
            data = json.load(f)
        # 确保是字典结构，并包含基本字段
        if isinstance(data, dict) and 'cookies' in data and 'localStorage' in data:
            return True
        else:
            return False
    except (json.JSONDecodeError, UnicodeDecodeError, FileNotFoundError):
        return False

# 保存登录状态数据
def save_login_data(driver, filename=STATE_FILE):
    # 获取 cookies
    cookies = driver.get_cookies()

    # 获取 localStorage 和 sessionStorage
    localStorage = driver.execute_script("return window.localStorage;")
    sessionStorage = driver.execute_script("return window.sessionStorage;")

    data = {
        "cookies": cookies,
        "localStorage": dict(localStorage),
        "sessionStorage": dict(sessionStorage)
    }

    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)

    print("✅ 登录信息已保存")

# 加载登录状态数据
def load_login_data(driver, filename=STATE_FILE):
    with open(filename, 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 设置 cookies
    for cookie in data["cookies"]:
        # 清除部分可能导致问题的字段
        cookie.pop('sameSite', None)
        cookie.pop('expiry', None)
        cookie.pop('httpOnly', None)
        cookie.pop('secure', None)
        try:
            driver.add_cookie(cookie)
        except Exception as e:
            print("⚠️ 添加 Cookie 出错：", e)

    # 设置 localStorage 和 sessionStorage
    def set_storage(storage_dict, storage_type="localStorage"):
        script = ""
        for key, value in storage_dict.items():
            script += f"window.{storage_type}.setItem('{key}', '{value}');"
        if script:
            driver.execute_script(script)

    set_storage(data["localStorage"], "localStorage")
    set_storage(data["sessionStorage"], "sessionStorage")

    # 刷新页面以应用登录状态
    driver.get("https://www.zhipin.com/")
    print("✅ 登录状态已恢复")

# 主流程
if has_login_state():
    try:
        load_login_data(driver)
    except Exception as e:
        print("⚠️ 加载登录状态失败，可能文件损坏或已过期：", e)
        print("请手动重新登录...")
        input("登录完成后按回车继续...")
        save_login_data(driver)
else:
    print("请手动登录账户，登录完成后按回车键继续...")
    input()  # 等待用户登录
    save_login_data(driver)

# 在退出前再次保存最新的状态数据
print("🔄 正在退出前再次保存最新的登录状态...")
save_login_data(driver)

# 关闭浏览器
driver.quit()
print("👋 浏览器已关闭")
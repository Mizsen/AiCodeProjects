import os
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options

# 获取当前脚本所在目录
current_dir = os.path.dirname(os.path.abspath(__file__))

# 构建 chrome.exe 和 chromedriver.exe 的相对路径
chrome_binary_path = os.path.join(current_dir, "chrome114-win64", "chrome.exe")
chromedriver_path = os.path.join(current_dir, "chrome114-win64", "chromedriver.exe")

# 创建 ChromeOptions 并指定浏览器位置
chrome_options = Options()
chrome_options.binary_location = chrome_binary_path

# 创建服务
service = Service(executable_path=chromedriver_path)

# 启动浏览器
driver = webdriver.Chrome(service=service, options=chrome_options)

# 打开网页
driver.get("https://www.zhipin.com/")

input("按回车键关闭浏览器...")

# 关闭浏览器
driver.quit()
import sys
import json
from PyQt5.QtWidgets import QApplication, QWidget, QVBoxLayout, QHBoxLayout, QLabel, QComboBox, QPushButton, QTextEdit
from PyQt5.QtCore import Qt


class CitySelector(QWidget):
    def __init__(self):
        super().__init__()
        self.city_data = None
        self.province_combo = None
        self.city_combo = None
        self.district_combo = None
        self.get_code_button = None
        self.code_display = None
        self.init_ui()
        self.load_city_data()
        self.setup_connections()

    def init_ui(self):
        self.setWindowTitle('城市选择器')
        self.setGeometry(300, 300, 500, 300)

        # 创建布局
        layout = QVBoxLayout()
        
        # 省份选择
        province_layout = QHBoxLayout()
        province_label = QLabel('省份:')
        self.province_combo = QComboBox()
        province_layout.addWidget(province_label)
        province_layout.addWidget(self.province_combo)
        
        # 地级市选择
        city_layout = QHBoxLayout()
        city_label = QLabel('地级市:')
        self.city_combo = QComboBox()
        city_layout.addWidget(city_label)
        city_layout.addWidget(self.city_combo)
        
        # 区县选择
        district_layout = QHBoxLayout()
        district_label = QLabel('区县:')
        self.district_combo = QComboBox()
        district_layout.addWidget(district_label)
        district_layout.addWidget(self.district_combo)
        
        # 按钮
        button_layout = QHBoxLayout()
        self.get_code_button = QPushButton('获取地级市代码')
        button_layout.addStretch()
        button_layout.addWidget(self.get_code_button)
        button_layout.addStretch()
        
        # 代码显示文本框
        self.code_display = QTextEdit()
        self.code_display.setMaximumHeight(50)
        self.code_display.setReadOnly(True)
        
        # 添加到主布局
        layout.addLayout(province_layout)
        layout.addLayout(city_layout)
        layout.addLayout(district_layout)
        layout.addLayout(button_layout)
        layout.addWidget(self.code_display)
        
        self.setLayout(layout)

    def load_city_data(self):
        """加载城市数据"""
        try:
            with open('city.json', 'r', encoding='utf-8') as f:
                data = json.load(f)
                self.city_data = data['zpData']['cityList']
                self.populate_provinces()
        except FileNotFoundError:
            print("未找到city.json文件")
        except json.JSONDecodeError:
            print("city.json文件格式错误")
        except KeyError:
            print("city.json文件结构不符合预期")

    def populate_provinces(self):
        """填充省份下拉列表"""
        if not self.city_data:
            return
            
        self.province_combo.clear()
        for province in self.city_data:
            code = province.get('code', '')
            display_text = f"{province['name']} ({code})"
            self.province_combo.addItem(display_text, province)
        
        # 默认选择第一个省份
        if self.province_combo.count() > 0:
            self.province_combo.setCurrentIndex(0)
            self.update_cities()

    def update_cities(self):
        """更新地级市下拉列表"""
        self.city_combo.clear()
        self.district_combo.clear()
        current_province_data = self.province_combo.currentData()
        
        if current_province_data and current_province_data.get('subLevelModelList'):
            cities = current_province_data['subLevelModelList']
            for city in cities:
                code = city.get('code', '')
                display_text = f"{city['name']} ({code})"
                self.city_combo.addItem(display_text, city)
        
        # 默认选择第一个地级市
        if self.city_combo.count() > 0:
            self.city_combo.setCurrentIndex(0)
            self.update_districts()

    def update_districts(self):
        """更新区县下拉列表"""
        self.district_combo.clear()
        current_city_data = self.city_combo.currentData()
        
        if current_city_data and current_city_data.get('subLevelModelList'):
            districts = current_city_data['subLevelModelList']
            for district in districts:
                if district.get('subLevelModelList') is None:  # 确保是区县级别
                    code = district.get('code', '')
                    display_text = f"{district['name']} ({code})"
                    self.district_combo.addItem(display_text, district)

    def get_city_code(self):
        """获取并显示地级市代码"""
        current_city_data = self.city_combo.currentData()
        if current_city_data:
            city_code = current_city_data.get('code', '')
            self.code_display.setPlainText(str(city_code))

    def setup_connections(self):
        """设置信号连接"""
        self.province_combo.currentIndexChanged.connect(self.update_cities)
        self.city_combo.currentIndexChanged.connect(self.update_districts)
        self.get_code_button.clicked.connect(self.get_city_code)


def main():
    app = QApplication(sys.argv)
    selector = CitySelector()
    selector.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()
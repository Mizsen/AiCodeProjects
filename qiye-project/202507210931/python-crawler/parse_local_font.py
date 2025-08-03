from fontTools.ttLib import TTFont

def get_local_font_mapping(local_font_path):
    """
    解析本地字体文件，建立特殊字符映射关系
    :param local_font_path: 本地字体文件路径（如.woff2）
    :return: 字符映射字典
    """
    # 加载本地字体文件
    font = TTFont(local_font_path)
    
    # 可选：将字体信息保存为XML，方便手动分析字形对应关系
    font.saveXML("local_font_info.xml")
    
    # 关键步骤：手动建立映射（需根据字体文件实际内容修改）
    # 以下为示例，实际映射需通过分析local_font_info.xml或字体工具获得
    # mapping = {
    #     "": "1",   # 假设该特殊字符对应数字1
    #     "": "4",   # 假设该特殊字符对应数字4
    #     "": "2"    # 假设该特殊字符对应数字2
    #     # 其他字符按实际情况补充
    # }


    mapping = {
    # Unicode字符 → 实际显示的数字
    "\ue031": "0",  # uniE031 字形为圆形，对应数字0
    "\ue032": "1",  # uniE032 字形为竖线，对应数字1
    "\ue033": "2",  # uniE033 字形为上弯曲线，对应数字2
    "\ue034": "3",  # uniE034 字形为上弯后再弯，对应数字3
    "\ue035": "4",  # uniE035 字形为左竖加斜杠，对应数字4
    "\ue036": "5",  # uniE036 字形为下弯曲线，对应数字5
    "\ue037": "6",  # uniE037 字形为下弯加竖线，对应数字6
    "\ue038": "7",  # uniE038 字形为横加竖，对应数字7
    "\ue039": "8",  # uniE039 字形为两个圆圈，对应数字8
    "\ue03a": "9"   # uniE03a 字形为圆圈加竖线，对应数字9
}
    
    return mapping

def parse_salary(salary_str, mapping):
    """替换薪资字符串中的特殊字符"""
    for special_char, actual_char in mapping.items():
        salary_str = salary_str.replace(special_char, actual_char)
    return salary_str

if __name__ == "__main__":
    # 本地字体文件路径（替换为你的字体文件实际路径）
    local_font_path = "3kovsijnt11693967587313.woff2"
    
    # 获取映射关系
    char_mapping = get_local_font_mapping(local_font_path)
    
    # 待解析的薪资字符串
    target_salary = "-K"
    
    # 解析结果
    result = parse_salary(target_salary, char_mapping)
    print(f"原始字符: {target_salary}")
    print(f"解析后: {result}")  # 例如：14-21K

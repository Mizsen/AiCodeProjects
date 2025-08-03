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


# 使用示例
if __name__ == "__main__":
    parser = FontParser()
    
    # 测试示例1
    result1 = parser.parse_text("-K")
    # 可以直接使用返回的结果进行后续处理
    print(f"后续处理使用：{result1['parsed_result']}\n")
    
    # 测试示例2
    result2 = parser.parse_text("-K")
    print(f"后续处理使用：{result2['parsed_result']}\n")
    
    # 自定义输入测试
    custom_text = input("请输入要测试的字符: ")
    result3 = parser.parse_text(custom_text)
    print(f"后续处理使用：{result3['parsed_result']}")

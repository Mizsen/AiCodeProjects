<template>
  <div class="image-uploader">
    <!-- 提示信息 -->
    <p v-if="showTip" class="tip">
      提示：部分浏览器仅支持单张上传，可多次点击“选择图片”按钮上传多张。
    </p>

    <!-- 文件选择按钮 -->
    <label class="upload-label">
      <input
        type="file"
        :multiple="isMultipleSupported"
        @change="handleFileUpload"
        accept="image/*"
        hidden
      />
      <span class="upload-btn">选择图片</span>
    </label>

    <!-- 图片预览区域 -->
    <div class="image-preview" v-if="images.length">
      <div class="preview-container">
        <div v-for="(image, index) in images" :key="index" class="preview-item">
          <img :src="image" class="preview-image" />
          <span class="remove-btn" @click="removeImage(index)">×</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ImageUploader',
  data() {
    return {
      images: [], // 存储图片预览地址
      fileList: [], // 存储原始 File 对象
      isMultipleSupported: true, // 是否支持 multiple 属性
      showTip: false // 是否显示提示信息
    };
  },
  created() {
    this.checkMultipleSupport();
  },
  methods: {
    // 检查浏览器是否支持 multiple 属性
    checkMultipleSupport() {
      const input = document.createElement('input');
      input.type = 'file';
      this.isMultipleSupported = input.multiple !== undefined;
      this.showTip = !this.isMultipleSupported; // 如果不支持，则显示提示
    },

    // 处理文件上传事件
    handleFileUpload(event) {
      const files = Array.from(event.target.files);
      if (!files.length) return;

      // 遍历文件，读取为 base64 预览图
      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.images.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });

      // 累加到文件列表中
      this.fileList.push(...files);

      // 触发上传事件给父组件
      this.$emit('upload', this.fileList);

      // 清空 input 的值，以便下次可以再次选择相同的文件
      event.target.value = null;
    },

    // 删除指定索引的图片
    removeImage(index) {
      this.images.splice(index, 1);
      this.fileList.splice(index, 1);
      this.$emit('upload', this.fileList);
    }
  }
};
</script>

<style scoped>
.image-uploader {
  margin: 20px 0;
}

.tip {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.upload-label {
  display: inline-block;
  cursor: pointer;
}

.upload-btn {
  display: inline-block;
  background: #409EFF;
  color: #fff;
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 15px;
  transition: background 0.2s;
  cursor: pointer;
}

.upload-btn:hover {
  background: #66b1ff;
}

.image-preview {
  margin-top: 12px;
}

.preview-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px #f0f1f2;
  background: #fafbfc;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 2px;
  right: 6px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  font-size: 16px;
  cursor: pointer;
  z-index: 2;
  transition: background 0.2s;
}

.remove-btn:hover {
  background: #f56c6c;
}
</style>
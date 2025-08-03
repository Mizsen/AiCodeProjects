<template>
  <div class="image-uploader">
    <label class="upload-label">
      <input type="file" multiple @change="handleFileUpload" accept="image/*" hidden />
      <span class="upload-btn">选择图片</span>
    </label>
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
  data() {
    return {
      images: [],
      fileList: []
    };
  },
  methods: {
    handleFileUpload(event) {
      const files = Array.from(event.target.files);
      this.images = [];
      this.fileList = files;
      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.images.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });
      this.$emit('upload', files);
    },
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
  background: rgba(0,0,0,0.5);
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
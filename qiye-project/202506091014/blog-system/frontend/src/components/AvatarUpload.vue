<template>
  <div class="avatar-upload">
    <div class="avatar-container" @click="triggerFileInput">
      <img v-if="avatarUrl" :src="avatarUrl" alt="用户头像" class="avatar-image" />
      <div v-else class="avatar-placeholder">
        <i class="el-icon-user-solid"></i>
      </div>
      <div class="avatar-overlay">
        <i class="el-icon-camera"></i>
        <span>点击上传</span>
      </div>
    </div>
    <input
      type="file"
      ref="fileInput"
      accept="image/*"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadUserAvatar, getUserAvatarUrl } from '../api/user'
import { useUserStore } from '../store/user'




export default {
  name: 'AvatarUpload',
  props: {
    userId: {
      type: [Number, String],
      required: true
    },
    initialAvatarUrl: {
      type: String,
      default: ''
    }
  },
  emits: ['upload-success'],
  setup(props, { emit }) {

    const userStore = useUserStore()
    const fileInput = ref(null)
    const avatarUrl = ref(props.initialAvatarUrl || getUserAvatarUrl(userStore.userInfo.avatarUrl))

    const triggerFileInput = () => {
      fileInput.value.click()
    }

    const handleFileChange = async (event) => {
      const file = event.target.files[0]
      if (!file) return

      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片文件')
        return
      }

      // 检查文件大小（1MB）
      if (file.size > 1024 * 1024) {
        ElMessage.error('图片大小不能超过1MB')
        return
      }

      try {
        await uploadUserAvatar(props.userId, file)
        ElMessage.success('头像上传成功')
        // 更新头像URL，添加时间戳防止缓存
        avatarUrl.value = `${getUserAvatarUrl(props.userId)}?t=${new Date().getTime()}`
        emit('upload-success', avatarUrl.value)
      } catch (error) {
        console.error('上传头像失败:', error)
        ElMessage.error(error.response?.data || '上传头像失败')
      }

      // 清空文件输入，允许重复选择同一文件
      event.target.value = ''
    }

    onMounted(() => {
      // 如果有初始头像URL，添加时间戳防止缓存
      if (props.initialAvatarUrl) {
        avatarUrl.value = `${props.initialAvatarUrl}?t=${new Date().getTime()}`
      }
    })

    return {
      fileInput,
      avatarUrl,
      triggerFileInput,
      handleFileChange
    }
  }
}
</script>

<style scoped>
.avatar-upload {
  display: inline-block;
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  background-color: #f5f7fa;
  border: 2px dashed #dcdfe6;
  transition: all 0.3s;
}

.avatar-container:hover {
  border-color: #409eff;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #909399;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay i {
  font-size: 24px;
  margin-bottom: 5px;
}

.avatar-overlay span {
  font-size: 14px;
}
</style> 
<template>
  <div class="post-create-page">
    <Navbar />
    <el-card class="post-create-card">
      <h2>发布博客</h2>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit">发布</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import Navbar from '../components/Navbar.vue'
import { createPost } from '../api/post'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ title: '', content: '' })

onMounted(async () => {
  if (!userStore.token) {
    window.$message ? window.$message.warning('请先登录') : alert('请先登录')
    router.push('/login')
    return
  }
  // 确保用户信息是最新的
  await userStore.fetchUserInfo()
})

const onSubmit = async () => {
  if (!form.value.title || !form.value.content) {
    window.$message ? window.$message.error('标题和内容不能为空') : alert('标题和内容不能为空')
    return
  }
  const res = await createPost(form.value)
  if (res && res.data) {
    window.$message ? window.$message.success('发布成功') : alert('发布成功')
    router.push('/')
  } else {
    window.$message ? window.$message.error('发布失败') : alert('发布失败')
  }
}
</script>

<style scoped>
.post-create-page {
  background: #18181c;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.post-create-card {
  width: 500px;
  margin-top: 80px;
}
</style>

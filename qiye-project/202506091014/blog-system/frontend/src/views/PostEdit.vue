<template>
  <div class="post-edit-page">
    <Navbar />
    <el-card class="post-edit-card">
      <h2>编辑博客</h2>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit">更新</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import Navbar from '../components/Navbar.vue'
import { getPost, updatePost } from '../api/post'

const router = useRouter()
const route = useRoute()
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
  
  // 获取博客信息
  try {
    const { data } = await getPost(route.params.id)
    // 检查是否是作者
    if (data.authorId !== userStore.userInfo?.id) {
      window.$message ? window.$message.error('您没有权限编辑此博客') : alert('您没有权限编辑此博客')
      router.push('/')
      return
    }
    form.value = data
  } catch (error) {
    window.$message ? window.$message.error(error.response?.data || '获取博客信息失败') : alert(error.response?.data || '获取博客信息失败')
    router.push('/')
  }
})

const onSubmit = async () => {
  if (!form.value.title || !form.value.content) {
    window.$message ? window.$message.error('标题和内容不能为空') : alert('标题和内容不能为空')
    return
  }

  try {
    await updatePost(route.params.id, form.value)
    window.$message ? window.$message.success('更新成功') : alert('更新成功')
    router.push(`/posts/${route.params.id}`)
  } catch (error) {
    window.$message ? window.$message.error(error.response?.data || '更新失败') : alert(error.response?.data || '更新失败')
  }
}
</script>

<style scoped>
.post-edit-page {
  background: #18181c;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.post-edit-card {
  width: 500px;
  margin-top: 80px;
}
</style>

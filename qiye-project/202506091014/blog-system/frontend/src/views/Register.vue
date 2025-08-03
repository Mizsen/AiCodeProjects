<template>
  <div class="register-page">
    <Navbar />
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form :model="form" @submit.prevent="onRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="login-link">
        已有账号？<router-link to="/login">登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../components/Navbar.vue'
import { useRouter } from 'vue-router'
import { register } from '../api/user'

const router = useRouter()
const form = ref({ username: '', email: '', password: '' })

const onRegister = async () => {
  const res = await register(form.value)
  if (res.data) {
    router.push('/login')
  }
}
</script>

<style scoped>
.register-page {
  background: #18181c;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.register-card {
  width: 350px;
  margin-top: 80px;
}
.login-link {
  margin-top: 16px;
  text-align: center;
}
</style>

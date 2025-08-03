<template>
  <div class="login-page">
    <Navbar />
    <el-card class="login-card">
      <h2>登录</h2>
      <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon style="margin-bottom: 16px;" @close="errorMsg = ''" />
      <el-form :model="form" @submit.prevent="onLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="register-link">
        还没有账号？<router-link to="/register">注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../components/Navbar.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { login } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ username: '', password: '' })
const errorMsg = ref('')

const onLogin = async () => {
  if (!form.value.username || !form.value.password) {
    errorMsg.value = '用户名和密码不能为空'
    return
  }
  try {
    const res = await login(form.value)
    if (res.data && res.data.token) {
      userStore.setToken(res.data.token)
      await userStore.fetchUserInfo()
      router.push('/')
    } else {
      let msg = ''
      if (typeof res.data === 'string') {
        msg = res.data
      } else if (res.data && res.data.msg) {
        msg = res.data.msg
      } else {
        msg = '登录失败'
      }
      errorMsg.value = msg
    }
  } catch (err) {
    // 处理400等错误
    let msg = ''
    if (err.response && typeof err.response.data === 'string') {
      msg = err.response.data
    } else if (err.response && err.response.data && err.response.data.msg) {
      msg = err.response.data.msg
    } else {
      msg = '登录失败'
    }
    errorMsg.value = msg
  }
}
</script>

<style scoped>
.login-page {
  background: #18181c;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 350px;
  margin-top: 80px;
}
.register-link {
  margin-top: 16px;
  text-align: center;
}
</style>

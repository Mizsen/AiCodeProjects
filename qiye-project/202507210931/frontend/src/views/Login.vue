<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form :model="form" @submit.prevent="onLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onLogin">登录</el-button>
          <el-button link @click="$router.push('/register')">注册</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="error" type="error" :closable="false" :title="error" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { userApi } from '@/api/index';  // 保持原有路径不变

const router = useRouter();
const form = ref({ username: '', password: '' });
const error = ref('');

const onLogin = async () => {
  error.value = '';
  try {
    const res = await userApi.login(form.value);
    if (res.data && res.data.token && res.data.user) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.user));
      router.push('/');
    } else {
      error.value = '用户名或密码错误';
    }
  } catch (e) {
    error.value = '登录失败';
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}
.login-card {
  width: 350px;
  padding: 32px 24px;
}
</style>

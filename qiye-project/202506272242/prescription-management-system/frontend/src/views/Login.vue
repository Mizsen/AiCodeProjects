<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>登录</h2>
      <el-form :model="form" @submit.native.prevent="onLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onLogin" :loading="loading">登录</el-button>
          <el-button type="text" @click="$router.push('/register')">注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { authApi } from '@/api/index';
import { ElMessage } from 'element-plus';

const router = useRouter();
const store = useStore();
const form = ref({ username: '', password: '' });
const loading = ref(false);

const onLogin = async () => {
  loading.value = true;
  try {
    const { data } = await authApi.login(form.value);
    if (data.success && data.token) {
      localStorage.setItem('token', data.token); // 显式保存token
      await store.dispatch('login', data);
      router.push('/admin');
    } else {
      ElMessage.error(data.msg || '登录失败');
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; }
.login-card { width: 350px; }
</style>

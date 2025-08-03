<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>用户注册</h2>
      <el-form :model="form" @submit.prevent="onRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" autocomplete="email" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onRegister">注册</el-button>
          <el-button link @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="error" type="error" :closable="false" :title="error" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { userApi } from '@/api/index';

const router = useRouter();
const form = ref({ username: '', email: '', password: '' });
const error = ref('');

const onRegister = async () => {
  error.value = '';
  try {
    const res = await userApi.register(form.value);
    if (res.data && res.data.id) {
      router.push('/login');
    } else {
      error.value = '注册失败';
    }
  } catch (e) {
    error.value = '注册失败';
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}
.register-card {
  width: 350px;
  padding: 32px 24px;
}
</style>

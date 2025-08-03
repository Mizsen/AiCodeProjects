<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form :model="form" @submit.native.prevent="onRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="医生" value="doctor" />
            <el-option label="药师" value="pharmacist" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onRegister" :loading="loading">注册</el-button>
          <el-button type="text" @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/api/index';
import { ElMessage } from 'element-plus';

const router = useRouter();
const form = ref({ username: '', password: '', realName: '', role: '' });
const loading = ref(false);

const onRegister = async () => {
  loading.value = true;
  try {
    const { data } = await authApi.register(form.value);
    if (data.success) {
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    } else {
      ElMessage.error(data.msg || '注册失败');
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '注册失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container { display: flex; justify-content: center; align-items: center; height: 100vh; }
.register-card { width: 400px; }
</style>

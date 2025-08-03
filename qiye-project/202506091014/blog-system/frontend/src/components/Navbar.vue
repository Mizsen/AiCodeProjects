<template>
  <el-header>
    <el-menu :default-active="active" mode="horizontal" @select="handleSelect">
      <el-menu-item index="home">首页</el-menu-item>
      <el-menu-item index="login" v-if="!isLoggedIn">登录</el-menu-item>
      <el-menu-item index="register" v-if="!isLoggedIn">注册</el-menu-item>
      <el-menu-item index="myblog" v-if="isLoggedIn">个人博客</el-menu-item>      
      <el-submenu v-if="isLoggedIn" index="user" class="custom-submenu">
        <template #title>{{ userInfo.nickname || userInfo.username }}</template>
        <el-menu-item index="profile" @click="showLogout = !showLogout">个人资料</el-menu-item>
        <el-menu-item v-show="showLogout" index="logout">登出</el-menu-item>
      </el-submenu>
    </el-menu>
  </el-header>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user'; 

const router = useRouter();
const userStore = useUserStore();

const active = ref('home');
const showLogout = ref(false);

const isLoggedIn = computed(() => !!userStore.token);
const userInfo = computed(() => userStore.userInfo || {});

function handleSelect(key) {
  if (key === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (key === 'profile') {
    router.push(`/users/${userStore.userInfo.id}`);
  } else if (key === 'myblog') {
    router.push(`/users/${userStore.userInfo.id}`);
  } else {
    router.push(`/${key === 'home' ? '' : key}`);
  }
}
</script>

<style scoped>
.el-header {
  background-color: #409eff;
  color: white;
}

:deep(.el-menu-item[index="logout"]) {
  background-color: white !important;
  color: #333 !important;
}
</style>

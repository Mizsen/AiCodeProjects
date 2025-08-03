
<template>
  <div class="home-layout">
    <el-header class="menu-header">
      <el-menu :default-active="activeMenu" mode="horizontal" @select="onMenuSelect" style="flex:1;min-width:0;overflow:visible;">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/jobs">岗位列表</el-menu-item>
      </el-menu>
      <div class="user-info">
        <span>{{ user?.username }}</span>
        <el-button link @click="logout" type="danger">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const activeMenu = ref(route.path === '/' ? '/' : route.path.startsWith('/jobs') ? '/jobs' : '/');
const user = ref(null);

onMounted(() => {
  user.value = JSON.parse(localStorage.getItem('user') || 'null');
  const token = localStorage.getItem('token');
  if (!user.value || !token) {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    router.replace('/login');
  }
  activeMenu.value = route.path === '/' ? '/' : route.path.startsWith('/jobs') ? '/jobs' : '/';
});

const onMenuSelect = (path) => {
  activeMenu.value = path;
  router.push(path);
};

const logout = () => {
  localStorage.removeItem('user');
  localStorage.removeItem('token');
  router.push('/login');
};
</script>

<style scoped>
  .home-layout {
    min-height: 100vh;
    background: #f6f8fa;
    display: flex;
    flex-direction: column;
    width: 100%;
    max-width: none;
    margin: 0;
    box-sizing: border-box;
    overflow-x: hidden;
  }
  .menu-header {
    display: flex;
    align-items: center;
    gap: 24px;
    background: #fff;
    border-bottom: 1px solid #eee;
    padding: 0 32px;
    min-width: 0;
    width: 100%;
    box-sizing: border-box;
  }
  .menu-header .el-menu {
    flex: 1 1 0%;
    min-width: 0;
  }
  .el-main {
    flex: 1 1 auto;
    width: 100%;
    max-width: none;
    min-width: 0;
    margin: 0;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    padding: 0;
  }
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
  }
  @media (max-width: 600px) {
    .menu-header {
      flex-direction: column;
      align-items: flex-start;
      padding: 0 8px;
    }
    .user-info {
      margin-top: 8px;
    }
    .el-main {
      padding: 0;
    }
  }
</style>

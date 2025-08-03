<template>
  <div class="admin-layout">
    <el-container style="height: 100vh">
      <!-- 移动端：抽屉菜单 -->
      <el-drawer v-if="isMobile" v-model="drawerVisible" direction="ltr" :with-header="false" size="220px">
        <el-menu :default-active="activeMenu" :default-openeds="defaultOpeneds" class="el-menu-vertical-demo"
          :unique-opened="true" router @select="go">
          <el-sub-menu v-for="(group, idx) in menuGroups" :key="idx" :index="String(idx)">
            <template #title>{{ group.title }}</template>
            <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-drawer>
      <!-- PC端：侧边栏菜单 -->
      <el-aside v-else width="220px">
        <el-menu :default-active="activeMenu" :default-openeds="defaultOpeneds" class="el-menu-vertical-demo"
          :unique-opened="true" router>
          <el-sub-menu v-for="(group, idx) in menuGroups" :key="idx" :index="String(idx)">
            <template #title>{{ group.title }}</template>
            <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path" @click="go(item.path)">
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header style="background:#f5f5f5;display:flex;justify-content:space-between;align-items:center;">
          <div style="display:flex;align-items:center;">
            <el-button v-if="isMobile" icon="el-icon-menu" @click="drawerVisible = true" circle size="small"
              style="margin-right:8px;" />
            <span>欢迎，{{ user?.username }}（{{ user?.role }}）</span>
          </div>
          <el-button type="danger" @click="logout" size="small">退出系统</el-button>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';

const store = useStore();
const router = useRouter();
const route = useRoute();
const user = computed(() => store.getters.user);
const menu = computed(() => store.getters.menu);
const activeMenu = computed(() => route.path);

// 菜单映射：根据后端返回的菜单名生成二级菜单
const menuMap = {
  '药品管理': [
    { label: '新增药品', path: '/admin/drug/add' },
    { label: '药品列表', path: '/admin/drug/list' }
  ],
  '药方管理': [
    { label: '新增药方', path: '/admin/prescription/add' },
    { label: '药方列表', path: '/admin/prescription/list' }
  ],
  '用户管理': [
    { label: '用户列表', path: '/admin/user/list' }
  ]
};

const menuGroups = computed(() => {
  return menu.value.map(m => ({
    title: m,
    children: menuMap[m] || []
  }));
});

const defaultOpeneds = ref([]);
// 响应式：判断是否为移动端
const isMobile = ref(window.innerWidth < 768);
const drawerVisible = ref(false);

function handleResize() {
  isMobile.value = window.innerWidth < 768;
}
onMounted(() => {
  window.addEventListener('resize', handleResize);
  // 默认展开所有分组
  defaultOpeneds.value = menuGroups.value.map((_, idx) => String(idx));
});
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

function go(path) {
  if (isMobile.value) drawerVisible.value = false;
  router.push(path);
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('menu');
  localStorage.removeItem('username');
  store.dispatch('logout');
  router.push('/login');
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
</style>

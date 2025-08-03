<template>
  <div class="home-page">
    <Navbar />
    <div class="user-info-card" v-if="userStore.userInfo">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
          </div>
        </template>
        <div class="user-info">
          <p><strong>用户名：</strong>{{ userStore.userInfo.username }}</p>
          <p><strong>邮箱：</strong>{{ userStore.userInfo.email }}</p>
          <p><strong>注册时间：</strong>{{ new Date(userStore.userInfo.registerTime).toLocaleDateString() }}</p>
        </div>
      </el-card>
    </div>
    <div class="search-bar-wrapper">
      <SearchBar v-model="searchQuery" @search="onSearch" />
      <el-select v-model="selectedCategory" placeholder="分类筛选" style="width: 180px; margin-left: 16px;">
        <el-option label="全部" value="" />
        <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
      </el-select>
      <el-button type="primary" style="margin-left: 16px;" @click="goToCreate">发布博客</el-button>
    </div>
    <div class="post-list">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
    </div>
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="page"
      @current-change="handlePageChange"
      style="margin: 32px auto; text-align: center;"
    />





  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '../store/user'
import Navbar from '../components/Navbar.vue'
import SearchBar from '../components/SearchBar.vue'
import PostCard from '../components/PostCard.vue'
import { getPosts, searchPosts } from '../api/post'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const posts = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const searchQuery = ref('')
const selectedCategory = ref('')
const categories = ref(['技术', '生活', '随笔'])
const router = useRouter()
const userStore = useUserStore()

const fetchPosts = async (newPage ) => {
  page.value = newPage || page.value;
  const { data } = await getPosts(page.value, pageSize)
  posts.value = data.records || data.data || []
  total.value = data.total || 0
}

const onSearch = async (newPage) => {
  try {
    page.value = newPage || page.value;
    if (!searchQuery.value && !selectedCategory.value) {
      await fetchPosts(page.value);
      return;
    }
    
    const { data } = await searchPosts(
      searchQuery.value,
      selectedCategory.value,
      page.value,
      pageSize
    );
    
    posts.value = data.records || [];
    total.value = data.total || 0;
  } catch (error) {
    console.error('搜索失败:', error);
    ElMessage.error('搜索失败：' + error.message);
  }
}

const goToCreate = () => {
  router.push('/create')
}

const handlePageChange = (newPage) => {
  if (searchQuery.value || selectedCategory.value) {
    onSearch(newPage)
  } else {
    fetchPosts(newPage)
  }
}

// 监听分类变化
watch(selectedCategory, (newValue) => {
  if (searchQuery.value || newValue) {
    onSearch(1)
  } else {
    fetchPosts(1)
  }
})

onMounted(async () => {
  await fetchPosts()
  if (userStore.token) {
    await userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.home-page {
  background: #18181c;
  min-height: 100vh;
  color: #fff;
  padding: 20px;
}
.search-bar-wrapper {
  display: flex;
  align-items: center;
  margin: 24px 0 16px 0;
  justify-content: center;
}
.post-list {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  justify-content: center;
}
.user-info-card {
  max-width: 400px;
  margin: 0 auto 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.user-info p {
  margin: 8px 0;
}
</style>

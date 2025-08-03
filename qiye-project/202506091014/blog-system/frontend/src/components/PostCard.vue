<template>
  <el-card class="post-card" shadow="hover">
    <div class="post-header">
      <span class="post-title">{{ post.title }}</span>
    </div>
    <div class="post-summary">{{ post.summary || post.content?.slice(0, 80) + '...' }}</div>
    <div class="post-meta">
      <CircleAvatar :src="post.authorAvatar" :size="32" />
      <span class="author">{{ post.authorName }}</span>
      <span class="meta-item"><el-icon><i class="el-icon-thumb"></i></el-icon> {{ post.likeCount || 0 }}</span>
      <span class="meta-item"><el-icon><i class="el-icon-chat-dot-round"></i></el-icon> {{ post.commentCount || 0 }}</span>
    </div>
    <div class="post-footer">
      <router-link :to="`/posts/${post.id}`">
        <el-button type="primary" size="small">查看详情</el-button>
      </router-link>
      <!-- 如果是博客作者,显示编辑和删除按钮 -->
      <template v-if="isAuthor">
        <router-link :to="`/posts/${post.id}/edit`">
          <el-button type="warning" size="small" style="margin-left: 8px;">编辑</el-button>
        </router-link>
        <el-button type="danger" size="small" @click="handleDelete" style="margin-left: 8px;">删除</el-button>
      </template>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { deletePost } from '../api/post'
import CircleAvatar from './CircleAvatar.vue'

const props = defineProps({ post: Object })
const router = useRouter()
const userStore = useUserStore()

// 判断当前用户是否是博客作者
const isAuthor = computed(() => userStore.userInfo?.id === props.post.authorId)

// 处理删除操作
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇博客吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePost(props.post.id)
    window.$message ? window.$message.success('删除成功') : alert('删除成功')
    // 触发父组件刷新列表
    router.go(0)
  } catch (error) {
    if(error !== 'cancel') {
      window.$message ? window.$message.error(error.response?.data || '删除失败') : alert(error.response?.data || '删除失败')
    }
  }
}
</script>

<style scoped>
.post-card {
  width: 340px;
  margin: 0 8px 16px 8px;
  background: #23232b;
  color: #fff;
  border-radius: 12px;
  border: none;
  transition: box-shadow 0.2s;
}
.post-header {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 8px;
}
.post-summary {
  color: #bbb;
  margin-bottom: 12px;
  min-height: 40px;
}
.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.95rem;
  margin-bottom: 8px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.post-footer {
  text-align: right;
}
</style>

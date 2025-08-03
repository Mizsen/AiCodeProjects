<template>
  <el-card class="comment-card" shadow="never">
    <div class="comment-header">
      <CircleAvatar :src="comment.authorAvatar" :size="32" />
      <span class="nickname">{{ comment.authorName }}</span>
      <span class="date">{{ comment.createTime }}</span>
    </div>
    <div class="comment-content">
      <template v-if="comment.parent">
        <div class="reply-info">
          <CircleAvatar :src="comment.parent.authorAvatar" :size="24" />
          <span class="reply-name">@{{ comment.parent.authorName }}</span>
        </div>
      </template>
      {{ comment.content }}
    </div>
    <div class="comment-actions">
      <div class="action-buttons">
        <el-button size="small" text @click="toggleReplyInput">回复</el-button>
        <el-button size="small" text type="danger" v-if="canDelete" @click="$emit('delete', comment)">删除</el-button>
      </div>
      <div v-if="showReplyInput" class="reply-input">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="2"
          placeholder="写下你的回复..."
        />
        <div class="reply-buttons">
          <el-button size="small" @click="submitReply">发送</el-button>
          <el-button size="small" text @click="cancelReply">取消</el-button>
        </div>
      </div>
    </div>
    <!-- 显示回复评论 -->
    <div v-if="comment.replies && comment.replies.length > 0" class="replies-section">
      <CommentCard
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :can-delete="canDelete"
        @reply="handleReply"
        @delete="$emit('delete', $event)"
        class="reply-card"
      />
    </div>
  </el-card>
</template>

<script setup>
import CircleAvatar from './CircleAvatar.vue'
import { ref } from 'vue'

const props = defineProps({
  comment: Object,
  canDelete: Boolean
})

const emit = defineEmits(['reply', 'delete'])
const showReplyInput = ref(false)
const replyContent = ref('')

const toggleReplyInput = () => {
  showReplyInput.value = !showReplyInput.value
  if (!showReplyInput.value) {
    replyContent.value = ''
  }
}

const handleReply = (replyData) => {
  emit('reply', replyData)
}

const submitReply = () => {
  if (replyContent.value.trim()) {
    emit('reply', { 
      parentComment: props.comment,
      content: replyContent.value.trim() 
    })
    replyContent.value = ''
    showReplyInput.value = false
  }
}

const cancelReply = () => {
  replyContent.value = ''
  showReplyInput.value = false
}
</script>

<style scoped>
.comment-card {
  margin-bottom: 16px;
  background: #23232b;
  color: #fff;
  border-radius: 8px;
  border: none;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1rem;
  margin-bottom: 4px;
}
.nickname {
  font-weight: bold;
}
.date {
  color: #aaa;
  font-size: 0.9em;
  margin-left: 8px;
}
.comment-content {
  margin: 8px 0 4px 0;
  word-break: break-all;
}
.comment-actions {
  margin-top: 8px;
}
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}
.reply-input {
  margin-top: 12px;
}
.reply-buttons {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
.reply-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.05);
  padding: 4px 8px;
  border-radius: 4px;
}
.reply-name {
  color: #409EFF;
  font-size: 0.95em;
}
.replies-section {
  margin-top: 16px;
  padding-left: 24px;
  border-left: 2px solid rgba(255, 255, 255, 0.1);
}
.reply-card {
  margin-bottom: 12px;
}
</style>

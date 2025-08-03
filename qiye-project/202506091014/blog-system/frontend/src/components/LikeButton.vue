<template>
  <el-button
    :type="liked ? 'primary' : 'default'"
    :icon="liked ? 'el-icon-thumb' : 'el-icon-thumb'"
    @click="toggleLike"
    circle
    size="small"
  >
    <span style="margin-left: 4px">{{ count }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useUserStore } from '../store/user'
import { likePost, unlikePost } from '../api/like'

const userStore = useUserStore()
const props = defineProps({
  postId: [String, Number],
  liked: Boolean,
  count: Number
})
const emit = defineEmits(['update:liked', 'update:count'])
const isLiked = ref(props.liked)
const likeCount = ref(props.count)

watch(() => props.liked, v => (isLiked.value = v))
watch(() => props.count, v => (likeCount.value = v))

const toggleLike = async () => {
  if (!userStore.userInfo) {
    window.$message ? window.$message.warning('请先登录') : alert('请先登录')
    return
  }

  try {
    if (!isLiked.value) {
      await likePost(userStore.userInfo.id, props.postId)
      likeCount.value++
      isLiked.value = true
    } else {
      await unlikePost(userStore.userInfo.id, props.postId)
      likeCount.value--
      isLiked.value = false
    }
    emit('update:liked', isLiked.value)
    emit('update:count', likeCount.value)
  } catch (error) {
    window.$message ? window.$message.error(error.response?.data || '操作失败') : alert(error.response?.data || '操作失败')
  }
}
</script>

<template>
  <el-avatar :src="avatarSrc" :size="sizeMap[size]" :style="avatarStyle" />
</template>

<script setup>
import { computed } from 'vue'
import { getUserAvatarUrl } from '../api/user'

const props = defineProps({
  src: String,
  size: {
    type: String,
    default: 'medium',
    validator: v => ['small', 'medium', 'large', '32', '48', '64'].includes(v)
  }
})

const avatarSrc = computed(() => {
  if (!props.src) return getUserAvatarUrl('default')
  // 如果src是完整的文件名（包含.png等后缀），则使用getUserAvatarUrl
  if (props.src.includes('.')) {
    // const id = props.src.split('.')[0]
    return getUserAvatarUrl(props.src)
  }

  // 否则直接使用src
  return props.src
})

const sizeMap = {
  small: 32,
  medium: 48,
  large: 64,
  32: 32,
  48: 48,
  64: 64
}
const avatarStyle = {
  border: '2px solid #444',
  background: '#222'
}
</script>

<style scoped>
.el-avatar {
  transition: box-shadow 0.2s;
}
.el-avatar:hover {
  box-shadow: 0 0 8px #409eff;
  cursor: pointer;
}
</style>

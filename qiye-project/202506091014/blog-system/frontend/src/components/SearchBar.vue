<template>
  <div class="search-bar">
    <el-input
      v-model="searchText"
      placeholder="搜索博客..."
      class="search-input"
      @keyup.enter="handleSearch"
      :prefix-icon="'el-icon-search'"
      clearable
    >
      <template #append>
        <el-button @click="handleSearch">搜索</el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: String
})

const emit = defineEmits(['update:modelValue', 'search'])

const searchText = ref(props.modelValue || '')

watch(() => props.modelValue, (newValue) => {
  searchText.value = newValue
})

watch(searchText, (newValue) => {
  emit('update:modelValue', newValue)
})

const handleSearch = () => {
  emit('search')
}
</script>

<style scoped>
.search-bar {
  width: 100%;
  max-width: 500px;
}

.search-input {
  width: 100%;
}

:deep(.el-input-group__append) {
  background-color: #409EFF;
  border-color: #409EFF;
  color: white;
  padding: 0 16px;
}

:deep(.el-input-group__append:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

:deep(.el-button) {
  background: transparent;
  border: none;
  color: white;
  padding: 0;
}
</style>

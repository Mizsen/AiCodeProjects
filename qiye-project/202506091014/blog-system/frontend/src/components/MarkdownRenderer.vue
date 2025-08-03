<template>
  <div class="markdown-body" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
const props = defineProps({ content: String })
const renderedHtml = computed(() => props.content ? marked.parse(props.content) : '')
</script>

<style scoped>
.markdown-body {
  color: #fff;
  background: transparent;
  font-size: 1.1rem;
  line-height: 1.7;
  word-break: break-all;
}
.markdown-body img {
  max-width: 100%;
  border-radius: 6px;
  margin: 8px 0;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
</style>

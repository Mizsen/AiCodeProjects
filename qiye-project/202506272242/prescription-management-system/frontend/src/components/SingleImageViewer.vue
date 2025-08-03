<template>
  <div class="single-image-viewer">
    <img :src="src" alt="Image Viewer" class="image" />
  </div>
</template>

<script>
export default {
  name: 'SingleImageViewer',
  props: {
    src: {
      type: String,
      required: true
    }
  }
};
</script>

<style scoped>
.single-image-viewer {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.image {
  max-width: 100%;
  transition: transform 0.2s;
}
</style>

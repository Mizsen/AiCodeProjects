<template>
  <div class="drug-detail">
    <h1>{{ drug.drugName }}</h1>
    <p><strong>规格:</strong> {{ drug.specification }}</p>
    <p><strong>生产厂家:</strong> {{ drug.manufacturer }}</p>
    <p><strong>适应症:</strong> {{ drug.indications }}</p>
    <div class="image-gallery">
      <!-- <ImageViewer :images="drugImages" /> -->

      <SingleImageViewer v-for="image in drugImages" :key="image.id" :src="image.imagePath" />
    </div>
    <!-- <router-link to="/drug/list" class="back-button">返回药品列表</router-link> -->
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { drugApi } from '@/api/index.js'; // Assume this API function is defined
import ImageViewer from '@/components/ImageViewer.vue';
import SingleImageViewer from '@/components/SingleImageViewer.vue';       

export default {
  components: {
    // ImageViewer,
    SingleImageViewer,
  },
  setup() {
    const route = useRoute();
    const drug = ref({});
    const drugImages = ref([]);

    const fetchDrugDetail = async () => {
      const drugId = route.params.id;
      const response = await drugApi.getDrugDetail(drugId);
      drug.value = response.data.drug;
      drugImages.value = Array.isArray(response.data.images) ? response.data.images : [];
    };

    onMounted(fetchDrugDetail);

    return {
      drug,
      drugImages,
    };
  },
};
</script>

<style scoped>
.drug-detail {
  padding: 20px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
}

.back-button {
  margin-top: 20px;
  display: inline-block;
  text-decoration: none;
  color: #007bff;
}
</style>
<template>
  <div class="prescription-detail">
    <h1>{{ prescription.prescriptionName }}</h1>
    <p><strong>适应症:</strong> {{ prescription.indications }}</p>
    <p><strong>用法与用量:</strong> {{ prescription.usage }}</p>
    <p><strong>治疗周期:</strong> {{ prescription.treatmentCycle }}</p>

    <h2>关联药品</h2>
    <el-table :data="prescription.drugs" style="width: 100%; margin-bottom: 16px;">
      <el-table-column prop="id" label="药品ID" />
      <el-table-column prop="drugName" label="药品名称" />
      <el-table-column prop="specification" label="规格" />
      <el-table-column prop="manufacturer" label="生产厂家" :show-overflow-tooltip="true"/>
    </el-table>

    <h2>药方图片</h2>
    <div class="image-gallery">
      <SingleImageViewer v-for="img in prescription.images" :key="img.id" :src="img.imagePath || img.image_path" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { prescriptionApi } from '@/api/index.js';
import SingleImageViewer from '@/components/SingleImageViewer.vue';

export default {
  components: {
    SingleImageViewer,
  },
  setup() {
    const route = useRoute();
    const prescription = ref({});

    const fetchPrescriptionDetail = async () => {
      const response = await prescriptionApi.getPrescription(route.params.id);
      // 后端返回 { prescription, drugs, images }
      const data = response.data;
      prescription.value = {
        ...data.prescription,
        drugs: data.drugs || [],
        images: data.images || []
      };
    };

    onMounted(() => {
      fetchPrescriptionDetail();
    });

    return {
      prescription,
    };
  },
};
</script>

<style scoped>
.prescription-detail {
  padding: 20px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
}
</style>
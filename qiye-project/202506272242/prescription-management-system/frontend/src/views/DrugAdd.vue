<template>
  <div class="drug-add">
    <el-card class="form-card">
      <h2 style="margin-bottom: 24px;">新增药品</h2>
      <el-form @submit.prevent="submitForm" label-width="100px" :model="drug">
        <el-form-item label="药品名称" required>
          <el-input v-model="drug.drugName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="规格" required>
          <el-input v-model="drug.specification" placeholder="如 20mg/片" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input type="textarea" v-model="drug.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="适应症" required>
          <el-input type="textarea" v-model="drug.indications" placeholder="请输入适应症" />
        </el-form-item>
        <el-form-item label="药品图片">
          <!-- <ImageUploader @upload="handleImageUpload" /> -->
          <ImageUploaderMobile @upload="handleImageUpload" />

        </el-form-item>
        <el-form-item>
          <el-button type="danger" native-type="submit">新增药品</el-button>
        </el-form-item>
      </el-form>
      <!-- <div v-if="drugImages.length" style="margin-top: 16px;">
        <ImageViewer :images="drugImages" />
      </div> -->
    </el-card>
  </div>
</template>

<script>
// import ImageUploader from '@/components/ImageUploader.vue';
import ImageUploaderMobile from '@/components/ImageUploaderMobile.vue';
import ImageViewer from '@/components/ImageViewer.vue';
import { drugApi } from '@/api/index.js';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

export default {
  components: {
    ImageUploaderMobile,
    // ImageUploader,
    ImageViewer,
  },
  setup() {
    const drug = ref({
      drugName: '',
      specification: '',
      manufacturer: '',
      indications: '',
    });
    const drugImages = ref([]);
    const router = useRouter();

    const submitForm = async () => {
      try {
        const formData = new FormData();
        formData.append('drug', JSON.stringify(drug.value));
        drugImages.value.forEach((file, idx) => {
          formData.append('images', file);
        });
        // console.log('提交图片', drugImages.value);
        const res = await drugApi.addDrugWithImages(formData);
        if (res.data && res.data.success) {
          ElMessage.success(res.data.msg || '新增药品成功');
          const drugId = res.data.drugId || res.data.id;
          if (drugId) {
            router.push({ name: 'DrugDetail', params: { id: drugId } });
          }
        } else {
          ElMessage.error(res.data.msg || '新增药品失败');
        }
      } catch (e) {
        ElMessage.error('新增药品失败');
      }
    };

    const handleImageUpload = (images) => {
      drugImages.value = images;
    };

    return {
      drug,
      drugImages,
      submitForm,
      handleImageUpload,
    };
  },
};
</script>

<style scoped>
.drug-add {
  max-width: 600px;
  margin: 32px auto;
}
.form-card {
  padding: 32px 24px 24px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px #f0f1f2;
}
</style>

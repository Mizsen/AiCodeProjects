<template>
  <div class="prescription-edit">
    <h2>Edit Prescription</h2>
    <el-form @submit.prevent="submitForm" label-width="120px" :model="prescription">
      <el-form-item label="药方名称">
        <el-input v-model="prescription.prescriptionName" required />
      </el-form-item>
      <el-form-item label="适应症">
        <el-input type="textarea" v-model="prescription.indications" required />
      </el-form-item>
      <el-form-item label="用法">
        <el-input type="textarea" v-model="prescription.usage" required />
      </el-form-item>
      <el-form-item label="疗程">
        <el-input v-model="prescription.treatmentCycle" required />
      </el-form-item>
      <el-form-item label="要移除的药品">
        <el-select
          v-model="selectedDrugs"
          multiple
          filterable
          placeholder="请选择要移除的药品"
          style="width: 100%"
        >
          <el-option
            v-for="drug in drugs"
            :key="drug.id"
            :label="drug.drugName"
            :value="drug.id"
          />
        </el-select>
        <el-button
          type="danger"
          size="small"
          style="margin-left: 8px; vertical-align: middle"
          @click="deleteSelectedDrugs"
          :disabled="!selectedDrugs.length"
          >删除</el-button
        >
      </el-form-item>
      <el-form-item label="关联药品">
        <el-select
          v-model="selectedDrugsNew"
          multiple
          filterable
          placeholder="请选择关联药品"
          :value-key="'id'"
        >
          <el-option
            v-for="drug in drugsNew"
            :key="drug.id"
            :label="drug.drugName"
            :value="drug"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="药方图片预览">
        <ImageViewer :images="prescriptionImages" />
      </el-form-item>
      <el-form-item label="药方图片">
        <ImageUploader @upload="handleImageUpload" />
      </el-form-item>
      <el-form-item>
        <el-button type="danger" native-type="submit">更新药方</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import ImageUploader from "@/components/ImageUploader.vue";
import ImageViewer from "@/components/ImageViewer.vue";
import { useRoute, useRouter } from "vue-router";
import { prescriptionApi, drugApi } from "@/api/index.js";
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";

export default {
  components: {
    ImageUploader,
    ImageViewer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const prescription = ref({
      prescriptionName: "",
      indications: "",
      usage: "",
      treatmentCycle: "",
    });
    const selectedDrugs = ref([]);
    const selectedDrugsNew = ref([]); // 新的选中的药品
    const prescriptionImagesNew = ref([]); // 新的药方图片
    const prescriptionImages = ref([]); // 药方图片
    const drugs = ref([]);
    const drugsNew = ref([]);

    // 拉取药方详情
    const fetchPrescriptionDetail = async () => {
      const id = route.params.id;
      if (!id) return;
      const res = await prescriptionApi.getPrescription(id);
      const data = res.data;
      prescription.value = {
        prescriptionName: data.prescription.prescriptionName,
        indications: data.prescription.indications,
        usage: data.prescription.usage,
        treatmentCycle: data.prescription.treatmentCycle,
      };
      // selectedDrugs.value = data.drugs ? data.drugs.map(d => d.id) : [];
      drugs.value = data.drugs || [];
      prescriptionImages.value = data.images || [];
    };

    // 拉取药品列表
    const fetchDrugs = async () => {
      // 获取所有药品用于多选，假设药品总数不会太大
      const res = await drugApi.listDrugs({ page: 1, size: 1000 });
      drugsNew.value = res.data.list;
    };

    // 提交表单
    const submitForm = async () => {
      const id = route.params.id;
      try {
        await prescriptionApi.updatePrescription(id, {
          prescriptionName: prescription.value.prescriptionName,
          indications: prescription.value.indications,
          usage: prescription.value.usage,
          treatmentCycle: prescription.value.treatmentCycle,
          drug_ids: selectedDrugs.value,
          images: prescriptionImages.value,
        });
        ElMessage.success("更新成功");
        router.push({ name: "PrescriptionDetail", params: { id } });
      } catch (e) {
        ElMessage.error("更新失败，请重试");
      }
    };

    // 删除选中药品
    const deleteSelectedDrugs = async () => {
      const id = route.params.id;
      if (!selectedDrugs.value.length) return;
      // await prescriptionApi.deletePrescriptionDrugs(id, selectedDrugs.value);
      // 删除后刷新药方详情
      fetchPrescriptionDetail();
    };

    const handleImageUpload = (images) => {
      prescriptionImagesNew.value = images;
    };

    const cancel = () => {
      const id = route.params.id;
      router.push({ name: "PrescriptionDetail", params: { id } });
    };

    onMounted(() => {
      fetchDrugs();
      fetchPrescriptionDetail();
    });

    return {
      prescription,
      selectedDrugs,
      prescriptionImages,
      drugs,
      drugsNew,
      selectedDrugsNew,
      prescriptionImagesNew,
      submitForm,
      handleImageUpload,
      deleteSelectedDrugs,
      cancel,
    };
  },
};
</script>

<style scoped>
.prescription-edit {
  max-width: 600px;
  margin: auto;
}
</style>

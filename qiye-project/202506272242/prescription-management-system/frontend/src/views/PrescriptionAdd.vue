<template>
  <div class="prescription-add">
    <el-card class="form-card">
      <h2 style="margin-bottom: 24px">新增药方</h2>
      <el-form @submit.prevent="submitForm" label-width="100px" :model="prescription">
        <el-form-item label="药方名称" required>
          <el-input
            v-model="prescription.prescriptionName"
            placeholder="请输入药方名称"
          />
        </el-form-item>
        <el-form-item label="适应症" required>
          <el-input
            type="textarea"
            v-model="prescription.indications"
            placeholder="请输入适应症"
          />
        </el-form-item>
        <el-form-item label="用法用量" required>
          <el-input
            type="textarea"
            v-model="prescription.usage"
            placeholder="请输入用法用量"
          />
        </el-form-item>
        <el-form-item label="治疗周期" required>
          <el-input v-model="prescription.treatmentCycle" placeholder="如 5 天 / 疗程" />
        </el-form-item>
        <el-form-item label="关联药品">
          <el-select
            v-model="selectedDrugs"
            multiple
            filterable
            placeholder="请选择关联药品"
            :value-key="'id'"
          >
            <el-option
              v-for="drug in drugs"
              :key="drug.id"
              :label="drug.drugName"
              :value="drug"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="药方图片">
          <!-- <ImageUploader @upload="handleImageUpload" /> -->
            <ImageUploaderMobile @upload="handleImageUpload" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" native-type="submit">新增药方</el-button>
        </el-form-item>
      </el-form>
      <!-- <div v-if="prescriptionImages.length" style="margin-top: 16px">
        <ImageViewer :images="prescriptionImages" />
      </div> -->
    </el-card>
  </div>
</template>

<script>
// import ImageUploader from '@/components/ImageUploader.vue';
import ImageUploaderMobile from "@/components/ImageUploaderMobile.vue";
import ImageViewer from "@/components/ImageViewer.vue";
import { prescriptionApi, drugApi } from "@/api/index.js";
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";

export default {
  components: {
    // ImageUploader,
    ImageUploaderMobile,
    ImageViewer,
  },
  setup() {
    const prescription = ref({
      prescriptionName: "",
      indications: "",
      usage: "",
      treatmentCycle: "",
    });
    const selectedDrugs = ref([]);
    const prescriptionImages = ref([]);
    const drugs = ref([]);
    const router = useRouter();

    // 拉取药品列表
    const fetchDrugs = async () => {
      // 获取所有药品用于多选，假设药品总数不会太大
      const res = await drugApi.listDrugs({ page: 1, size: 1000 });
      drugs.value = res.data.list;
    };

    // 提交表单
    const submitForm = async () => {
      try {
        const formData = new FormData();
        formData.append("prescription", JSON.stringify(prescription.value));
        formData.append(
          "drugIds",
          JSON.stringify(selectedDrugs.value.map((d) => (d.id ? d.id : d)))
        );
        prescriptionImages.value.forEach((file) => {
          formData.append("images", file);
        });
        const res = await prescriptionApi.addPrescription(formData);
        if (res.data && res.data.success) {
          ElMessage.success(res.data.msg || "新增药方成功");
          // 跳转到详情页，假设返回的 prescriptionId 字段为新建药方id
          const prescriptionId = res.data.prescriptionId || res.data.id;
          if (prescriptionId) {
            router.push({ name: "PrescriptionDetail", params: { id: prescriptionId } });
          }
        } else {
          ElMessage.error(res.data.msg || "新增药方失败");
        }
      } catch (e) {
        ElMessage.error("新增药方失败");
      }
    };

    const handleImageUpload = (images) => {
      prescriptionImages.value = images;
    };

    onMounted(() => {
      fetchDrugs();
    });

    return {
      prescription,
      selectedDrugs,
      prescriptionImages,
      drugs,
      submitForm,
      handleImageUpload,
    };
  },
};
</script>

<style scoped>
.prescription-add {
  max-width: 600px;
  margin: 32px auto;
}
.form-card {
  padding: 32px 24px 24px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px #f0f1f2;
}
</style>

<template>
  <div class="drug-edit">
    <h2>Edit Drug</h2>
    <el-form :model="drug" ref="drugForm" label-width="120px" @submit.prevent>
      <el-form-item label="药品名称" prop="drugName">
        <el-input v-model="drug.drugName" required></el-input>
      </el-form-item>
      <el-form-item label="规格" prop="specification">
        <el-input v-model="drug.specification" required></el-input>
      </el-form-item>
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input type="textarea" v-model="drug.manufacturer" required></el-input>
      </el-form-item>
      <el-form-item label="适应症" prop="indications">
        <el-input type="textarea" v-model="drug.indications" required></el-input>
      </el-form-item>
      <el-form-item label="药品图片预览">
        <ImageViewer :images="drugImages" />
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="updateDrug">更新药品</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { useRoute, useRouter } from "vue-router";
import { drugApi } from "@/api/index.js"; // Assume this API function is defined
import { ElMessage } from "element-plus";
import ImageUploader from "@/components/ImageUploader.vue";
import ImageViewer from "@/components/ImageViewer.vue";
import { ref, onMounted } from "vue";

export default {
  components: {
    ImageUploader,
    ImageViewer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const drug = ref({
      drugName: "",
      specification: "",
      manufacturer: "",
      indications: "",
    });
    const drugImages = ref([]); // 药方图片

    //拉取药品详情
    const loadDrug = async () => {
      const drugId = route.params.id;
      // Fetch drug details from API and set to drug
      // Example: drug.value = await fetchDrugDetails(drugId);

      const res = await drugApi.getDrugDetail(drugId);
      const data = res.data;

      drug.value = {
        drugName: data.drug.drugName,
        specification: data.drug.specification,
        manufacturer: data.drug.manufacturer,
        indications: data.drug.indications,
      };

      drugImages.value = data.images || [];
    };

    const updateDrug = async () => {
      const drugId = route.params.id;

      try {
        const res = await drugApi.updateDrug(drugId,drug.value);
        ElMessage.success("更新成功");
        router.push({ name: "DrugDetail", params: { drugId } });
      } catch (e) {
        console.error("更新药品时发生错误：", e); // 打印完整错误信息
        ElMessage.error("更新失败，请重试");
      }
    };

    const cancel = () => {
      const drugId = route.params.id;
      router.push({ name: "DrugDetail", params: { drugId } });
    };

    onMounted(() => {
      loadDrug();
    });
    return {
      drugImages,
      drug,
      updateDrug,
      cancel,
    };
  },
};
</script>

<style scoped>
.drug-edit {
  padding: 20px;
}
</style>

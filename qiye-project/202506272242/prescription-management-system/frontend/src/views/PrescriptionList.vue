<template>
  <div class="prescription-list">
    <h2>药方列表</h2>
    <div class="search-bar">
      <el-input v-model="searchName" placeholder="药方名称关键词" style="width:200px;margin-right:8px;" clearable />
      <el-input v-model="searchIndications" placeholder="适应症关键词" style="width:200px;margin-right:8px;" clearable />
      <el-button size="small" type="primary" @click="fetchList" >搜索</el-button>
      <el-button size="small" @click="resetSearch" >重置</el-button>
    </div>
    <el-table :data="prescriptions" style="width: 100%">
      <el-table-column prop="id" label="药方ID" />
      <el-table-column prop="prescriptionName" label="药方名称" />
      <el-table-column prop="indications" label="适应症" :show-overflow-tooltip="true"/>
      <el-table-column prop="usage" label="用法用量" :show-overflow-tooltip="true"/>
      <el-table-column prop="treatmentCycle" label="治疗周期" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" type="primary"  @click="viewPrescription(scope.row.id)">查看</el-button>
          <el-button size="small" type="success"  @click="editPrescription(scope.row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deletePrescription(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="pageSize"
      :current-page="page"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { prescriptionApi } from '@/api/index.js';

export default {
  setup() {
    const router = useRouter();
    const prescriptions = ref([]);
    const total = ref(0);
    const page = ref(1);
    const pageSize = ref(10);
    const searchName = ref('');
    const searchIndications = ref('');

    const fetchList = async () => {
      const res = await prescriptionApi.listPrescriptions({
        page: page.value,
        size: pageSize.value,
        name: searchName.value,
        indications: searchIndications.value,
      });
      prescriptions.value = res.data.list;
      total.value = res.data.total;
    };

    const handlePageChange = (val) => {
      page.value = val;
      fetchList();
    };

    const resetSearch = () => {
      searchName.value = '';
      searchIndications.value = '';
      fetchList();
    };

    const editPrescription = (id) => {
      router.push(`/admin/prescription/edit/${id}`);
    };

    const viewPrescription = (id) => {
      router.push(`/admin/prescription/detail/${id}`);
    };

    const deletePrescription = async (id) => {

      
      if (confirm('确定要删除该药方吗？')) {
        await prescriptionApi.deletePrescription(id);
        fetchList();
      }
    };

    onMounted(() => {
      fetchList();
    });

    return {
      prescriptions,
      total,
      page,
      pageSize,
      searchName,
      searchIndications,
      fetchList,
      handlePageChange,
      resetSearch,
      editPrescription,
      viewPrescription,
      deletePrescription,
    };
  },
};
</script>

<style scoped>
.prescription-list {
  max-width: 1000px;
  margin: auto;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  background: #f8f9fa;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.search-bar input {
  flex: 1 1 200px;
  max-width: 240px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}
.search-bar input:focus {
  border-color: #409eff;
  outline: none;
}
.search-bar .el-button {
  min-width: 64px;
  border-radius: 6px;
}
</style>

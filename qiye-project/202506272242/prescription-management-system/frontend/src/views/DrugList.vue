<template>
  <div class="drug-list">
    <h2>药品列表</h2>
    <div class="search-bar">
      <input v-model="searchName" placeholder="药品名称关键词" />
      <input v-model="searchIndications" placeholder="适应症关键词" />
      <el-button size="small" type="primary" @click="fetchList">搜索</el-button>
      <el-button size="small" @click="resetSearch">重置</el-button>
    </div>
    <el-table :data="drugs" style="width: 100%">
      <el-table-column prop="id" label="药品ID" />
      <el-table-column prop="drugName" label="药品名称" />
      <el-table-column prop="specification" label="规格" />
      <el-table-column prop="manufacturer" label="生产厂家" :show-overflow-tooltip="true"/>
      <el-table-column prop="indications" label="适应症" :show-overflow-tooltip="true"/>

      <el-table-column label="操作">
        <template #default="scope">
      
            <el-button size="small" type="primary" @click="viewDrugDetail(scope.row.id)">查看</el-button>
            <el-button size="small" type="success" @click="editDrug(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDrug(scope.row.id)">删除</el-button>
         
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
import { drugApi } from '@/api/index.js';
import { ElInput } from 'element-plus';

export default {
  setup() {
    const router = useRouter();
    const drugs = ref([]);
    const total = ref(0);
    const page = ref(1);
    const pageSize = ref(10);
    const searchName = ref('');
    const searchIndications = ref('');

    const fetchList = async () => {
      const res = await drugApi.listDrugs({
        page: page.value,
        size: pageSize.value,
        name: searchName.value,
        indications: searchIndications.value,
      });
      drugs.value = res.data.list;
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

    const editDrug = (id) => {
      router.push(`/admin/drug/edit/${id}`);
    };

    const deleteDrug = async (id) => {
      if (confirm('确定要删除该药品吗？')) {
        await drugApi.deleteDrug(id);
        fetchList();
      }
    };

    const viewDrugDetail = (id) => {
      router.push(`/admin/drug/detail/${id}`);
    };

    onMounted(() => {
      fetchList();
    });

    return {
      drugs,
      total,
      page,
      pageSize,
      searchName,
      searchIndications,
      fetchList,
      handlePageChange,
      resetSearch,
      editDrug,
      deleteDrug,
      viewDrugDetail,
    };
  },
};
</script>

<style scoped>
.drug-list {
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

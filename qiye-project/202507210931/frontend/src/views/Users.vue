<template>
  <div>
    <el-table :data="users" style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="onEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="编辑用户">
      <el-form :model="form">
        <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" placeholder="不修改请留空" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { userApi } from '@/api/index';

const users = ref([]);
const dialogVisible = ref(false);
const form = ref({});

const onEdit = (row) => {
  form.value = { ...row, password: '' };
  dialogVisible.value = true;
};

const onSave = async () => {
  await userApi.updateUser(form.value.id, form.value);
  dialogVisible.value = false;
  loadUsers();
};

const onDelete = async (id) => {
  await userApi.deleteUser(id);
  loadUsers();
};

const loadUsers = async () => {
  const res = await userApi.getUsers();
  users.value = res.data;
};

onMounted(loadUsers);
</script>
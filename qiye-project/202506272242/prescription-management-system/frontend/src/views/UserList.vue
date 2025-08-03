<template>
  <div>
    <el-card>
      <h2>用户列表</h2>
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="username" label="用户名" />
        <!-- <el-table-column prop="password" label="密码" /> -->
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="enabled" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.enabled === 1 ? 'success' : 'danger'">
              {{ scope.row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" />
        <el-table-column prop="lastLoginTime" label="最后登录时间" />
        <el-table-column prop="lastLoginIp" label="最后登录IP" />
      </el-table>
      <el-pagination
        style="margin-top: 16px; text-align: right;"
        background
        layout="prev, pager, next, jumper, ->, total"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { userApi } from '@/api/index';

const users = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);

const fetchUsers = async () => {
  const { data } = await userApi.getUsers({ page: page.value, size: size.value });
  users.value = data.list || [];
  total.value = data.total || 0;
};

onMounted(fetchUsers);

function handlePageChange(val) {
  page.value = val;
  fetchUsers();
}
</script>

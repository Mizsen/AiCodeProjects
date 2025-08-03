<template>
 
  <div class="jobs-layout">
 
    <div class="jobs-list-panel">
    <div class="jobs-search-bar">
      <el-form :inline="true" :model="search" @submit.prevent="onSearch">
        <template v-if="searchBarExpand">
          <el-form-item label="岗位名称">
            <el-input v-model="search.title" placeholder="岗位名称" clearable />
          </el-form-item>
          <el-form-item label="岗位类型">
            <el-input v-model="search.query_type" placeholder="岗位类型" clearable />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="search.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" clearable />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="search.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" clearable />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="search.city" placeholder="城市" clearable />
          </el-form-item>
          <el-form-item label="公司">
            <el-input v-model="search.company" placeholder="公司名称" clearable />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="search.description" placeholder="岗位描述" clearable />
          </el-form-item>
        </template>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button type="text" @click="searchBarExpand = !searchBarExpand" style="margin-left: 8px;">
            {{ searchBarExpand ? '收起筛选' : '展开筛选' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
      
      <div class="jobs-list" ref="listRef" @scroll="onScroll">
        <el-card v-for="job in jobs" :key="job.id" class="job-card" @click="selectJob(job)">
          <div class="job-title">{{ job.title }}</div>
          <div class="job-info">
            <span>{{ job.location }}</span>
            <span>{{ job.salary }}</span>
            <span>{{ job.experience }}</span>
            <span>{{ job.education }}</span>
            <span>{{ job.company }}</span>
          </div>
        </el-card>
        <div v-if="loading" class="loading">加载中...</div>
        <div v-if="noMore" class="no-more">没有更多数据</div>
      </div>
    </div>
    <div class="jobs-detail-panel">
      <el-card v-if="selectedJob" class="job-detail-card">
        <div class="job-detail-row"><strong>岗位名称：</strong>{{ selectedJob.title }}</div>
        <div class="job-detail-row"><strong>岗位公司：</strong>{{ selectedJob.company }}</div>
        <div class="job-detail-row"><strong>岗位地点：</strong>{{ selectedJob.location }}</div>
        <div class="job-detail-row"><strong>岗位薪资：</strong>{{ selectedJob.salary }}</div>
        <div class="job-detail-row"><strong>岗位经验：</strong>{{ selectedJob.experience }}</div>
        <div class="job-detail-row"><strong>岗位学历：</strong>{{ selectedJob.education }}</div>
        <div class="job-detail-row"><strong>岗位描述：</strong>{{ selectedJob.description }}</div>
        <div class="job-detail-row"><strong>岗位URL：</strong><a :href="selectedJob.url" target="_blank">{{ selectedJob.url }}</a></div>
        <div class="job-detail-row"><strong>生成时间：</strong>{{ selectedJob.createdAt }}</div>
      </el-card>
      <div v-else class="job-detail-empty">请选择左侧岗位查看详情</div>
    </div>
  </div>
</template>

<script setup>

import { ref, reactive, onMounted } from 'vue';
import { jobApi } from '@/api/index';

const jobs = ref([]);
const page = ref(0);
const size = 10;
const loading = ref(false);
const noMore = ref(false);
const selectedJob = ref(null);
const search = reactive({ title: '', city: '', company: '', description: '', query_type: '', startDate: '', endDate: '' });
const listRef = ref(null);
const searchBarExpand = ref(false);


const fetchJobs = async (reset = false) => {
  if (loading.value || noMore.value) return;
  loading.value = true;
  try {
    // 只传递有值的参数，空字符串和 null 不传递
    const params = { page: page.value, size };
    Object.keys(search).forEach(key => {
      if (search[key] !== undefined && search[key] !== null && search[key] !== '') {
        params[key] = search[key];
      }
    });
    const res = await jobApi.getJobs(params);


    const data = res.data?.content || [];
    if (reset) {
      jobs.value = data;
      noMore.value = res.data.last || data.length < size;
    } else {
      jobs.value = jobs.value.concat(data);
      if (res.data.last || data.length < size) noMore.value = true;
    }
    loading.value = false;
  } catch {
    loading.value = false;
    noMore.value = true;
  }
};

const onSearch = () => {
  page.value = 0;
  noMore.value = false;
  fetchJobs(true);
};

const onScroll = (e) => {
  const el = e.target;
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 10) {
    if (!loading.value && !noMore.value) {
      page.value++;
      fetchJobs();
    }
  }
};

const selectJob = (job) => {
  selectedJob.value = job;
};

onMounted(() => {
  fetchJobs(true);
});
</script>

<style scoped>
.jobs-layout {
  display: flex;
  height: calc(100vh - 64px);
}
.jobs-list-panel {
  width: 400px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}
.jobs-search-bar {
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.jobs-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafbfc;
}
.job-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.job-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}
.job-info span {
  margin-right: 16px;
  color: #666;
}
.loading, .no-more {
  text-align: center;
  color: #999;
  margin: 16px 0;
}
.jobs-detail-panel {
  flex: 1;
  padding: 32px;
  background: #fff;
}
.job-detail-card {
  padding: 24px;
}
.job-detail-row {
  margin-bottom: 12px;
  font-size: 16px;
}
.job-detail-empty {
  color: #aaa;
  font-size: 18px;
  text-align: center;
  margin-top: 80px;
}
</style>

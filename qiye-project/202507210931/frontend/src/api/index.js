import axios from './request';

// 用户相关 API
export const userApi = {
  // 用户登录
  login: (data) => axios.post('/users/login', data),
  
  // 用户注册
  register: (data) => axios.post('/users/register', data),
  
  // 获取用户列表
  getUsers: () => axios.get('/users'),
  
  // 更新用户信息
  updateUser: (id, data) => axios.put(`/users/${id}`, data),
  
  // 删除用户
  deleteUser: (id) => axios.delete(`/users/${id}`)
};

// 职位相关 API
export const jobApi = {
  // 分页获取职位信息
  getJobs: (params) => axios.get('/jobs/repo-page', { params }),
  
  // 获取职位趋势数据
  getJobTrendData: (params) => axios.get('/jobs/chart-data-year', { params })
};
import axios from 'axios'
import { useUserStore } from '../store/user'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.request.use(config => {
  // 这里不能直接调用 useUserStore，需在组件/业务代码中注入 token
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

export default instance

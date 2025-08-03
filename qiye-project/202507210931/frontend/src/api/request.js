import axios from 'axios';


const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})


// 添加请求拦截器，自动加 token
instance.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
       config.headers.Authorization = token
    } else {
        // 如果没有 token，可以选择不设置 Authorization 或者清除之前的 token
        delete config.headers.Authorization;
    }
    return config;
}, error => Promise.reject(error));

// 添加响应拦截器，遇到 401 自动跳转登录页
instance.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);


export default instance;
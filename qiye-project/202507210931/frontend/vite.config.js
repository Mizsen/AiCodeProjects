import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],server: {
    host: '0.0.0.0', // 新增此行，允许外部访问
    port: 3001,
    open: true,
    proxy: {
      // 代理 API 请求到后端 ，用于前后端分离测试
      // "/api": {
      //     target: "http://localhost:8003", changeOrigin: true, rewrite: (path) => path.replace(/^\/api/, "/api"),
      // },
      // "/upload": {
      //     target: "http://localhost:8001", changeOrigin: true, // 不需要 rewrite，直接代理
      // },
    },
  }, resolve: {
    alias: {
      "@": "/src",
    },
  },
  build: {
    outDir: path.resolve(__dirname, '../springboot-backend/src/main/resources/static'),
    emptyOutDir: true,
  }
})

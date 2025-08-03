# Prescription Management System - Frontend

## Overview

This project is the frontend part of the Prescription Management System, designed to provide a userEntity-friendly interface for managing prescriptions and drugs. It is built using Vue 3 and Vite, ensuring a modern and efficient development experience.

## Features

- **User Authentication**: Secure login and registration process with role-based access control.
- **Prescription Management**: Create, edit, view, and delete prescriptions with associated drugs and images.
- **Drug Management**: Manage drug information, including adding new drugs and editing existing ones.
- **Image Uploading**: Support for uploading multiple images for prescriptions and drugs with real-time previews.
- **Responsive Design**: A clean and responsive UI that works on various devices.

## Project Structure

```
frontend
├── src
│   ├── api               # API service files
│   ├── assets            # Static assets (images, styles)
│   ├── components        # Vue components
│   ├── router            # Vue Router configuration
│   ├── store             # Vuex store for state management
│   ├── views             # Vue views for different pages
│   ├── App.vue           # Root component
│   └── main.js           # Entry point of the application
├── public
│   └── index.html        # Main HTML file
├── package.json          # NPM configuration
└── vite.config.js        # Vite configuration
```

## Getting Started

1. **Clone the repository**:
   ```
   git clone <repository-url>
   cd prescription-management-system/frontend
   ```

2. **Install dependencies**:
   ```
   npm install
   ```

3. **Run the application**:
   ```
   npm run serve
   ```

4. **Open your browser** and navigate to `http://localhost:3000` to view the application.

## 常见问题

### 访问 http://127.0.0.1:3000/ 出现 404 错误

**原因分析：**
- Vite 默认只监听 `localhost`，在某些云开发环境或容器中，`127.0.0.1` 可能无法直接访问。
- 也可能是 Vite 没有正确启动，或前端项目未编译成功。
- 还可能是 `index.html` 路径或 `public` 目录结构有误。

**解决方法：**

1. **确保 Vite 正常启动且无报错。**
   - 终端应显示 `Local: http://localhost:3000/` 或 `Network: http://127.0.0.1:3000/`。
   - 如果有端口冲突或报错，请先解决启动报错。

2. **尝试访问 `http://localhost:3000/` 而不是 `127.0.0.1`。**

3. **如在云开发环境（如 Codespaces、Cloud IDE），需将 Vite 配置为监听所有地址：**

   ```javascript
   // filepath: /workspaces/nodejs-demo/qiye-project/202506272242/prescription-management-system/frontend/vite.config.mjs
   // ...existing code...
   export default defineConfig({
     plugins: [vue()],
     server: {
       host: '0.0.0.0', // 新增此行，允许外部访问
       port: 3000,
       open: true,
     },
     // ...existing code...
   });
   ```

4. **重启前端服务：**
   ```
   npm run serve
   ```

5. **确认 public/index.html 存在且内容正确。**

6. **如仍无法访问，检查云开发平台端口映射设置。**

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.



npm prune


npm uninstall

rd /s /q  node_modules
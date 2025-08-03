import { createApp } from 'vue';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css'; // 必须引入样式
import router from './router';
import store from './store';

const app = createApp(App);
app.use(ElementPlus);
app.use(router);
app.use(store);
app.mount('#app');

// 没有问题。main.js 已正确引入 App.vue、router 和 ElementPlus，并挂载到 #app。
// 确保 router/index.js 存在且配置了首页和兜底路由，App.vue 有 <router-view />。
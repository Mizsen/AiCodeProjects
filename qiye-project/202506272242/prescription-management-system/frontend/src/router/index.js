import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  {
    path: '/admin',
    component: () => import('../views/Admin.vue'),
    children: [
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('../views/UserList.vue'),
      },
      {
        path: 'prescription/add',
        name: 'PrescriptionAdd',
        component: () => import('../views/PrescriptionAdd.vue'),
      },
      {
        path: 'prescription/edit/:id',
        name: 'PrescriptionEdit',
        component: () => import('../views/PrescriptionEdit.vue'),
      },
      {
        path: 'prescription/list',
        name: 'PrescriptionList',
        component: () => import('../views/PrescriptionList.vue'),
      },
      {
        path: 'prescription/detail/:id',
        name: 'PrescriptionDetail',
        component: () => import('../views/PrescriptionDetail.vue'),
      },
      {
        path: 'drug/add',
        name: 'DrugAdd',
        component: () => import('../views/DrugAdd.vue'),
      },
      {
        path: 'drug/edit/:id',
        name: 'DrugEdit',
        component: () => import('../views/DrugEdit.vue'),
      },
      {
        path: 'drug/list',
        name: 'DrugList',
        component: () => import('../views/DrugList.vue'),
      },
      {
        path: 'drug/detail/:id',
        name: 'DrugDetail',
        component: () => import('../views/DrugDetail.vue'),
      },
      // 你可以继续添加其他子页面
      // { path: 'another/path', name: 'AnotherName', component: ... },
    ],
  },
  { path: '/', redirect: '/admin/user/list' },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 路由守卫：未登录强制跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login');
  } else {
    next();
  }
});

export default router;
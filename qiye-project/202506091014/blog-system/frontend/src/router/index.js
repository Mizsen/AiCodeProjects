import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import PostDetail from '../views/PostDetail.vue';
import UserProfile from '../views/UserProfile.vue';
import PostCreate from '../views/PostCreate.vue';
import PostEdit from '../views/PostEdit.vue';

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/posts/:id', name: 'PostDetail', component: PostDetail },
  { path: '/users/:id', name: 'UserProfile', component: UserProfile },
  { path: '/create', name: 'PostCreate', component: PostCreate },
  { path: '/posts/:id/edit', name: 'PostEdit', component: PostEdit },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;

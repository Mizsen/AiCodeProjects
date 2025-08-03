import { createStore } from 'vuex';
import { authApi } from '@/api/index';

const state = {
  token: localStorage.getItem('token') || '',
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  menu: JSON.parse(localStorage.getItem('menu') || '[]'),
  currentPrescription: null,
  prescriptionImages: [],
  drugImages: {}
};

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token;
    localStorage.setItem('token', token);
  },
  SET_USER(state, user) {
    state.user = user;
    localStorage.setItem('user', JSON.stringify(user));
  },
  SET_MENU(state, menu) {
    state.menu = menu;
    localStorage.setItem('menu', JSON.stringify(menu));
  },
  LOGOUT(state) {
    state.token = '';
    state.user = null;
    state.menu = [];
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('menu');
  },
  SET_CURRENT_PRESCRIPTION(state, data) {
    state.currentPrescription = data;
  },
  ADD_PRESCRIPTION_IMAGE(state, image) {
    state.prescriptionImages.push(image);
  },
  SET_DRUG_IMAGES(state, images) {
    state.drugImages = images;
  }
};

const actions = {
  async login({ commit }, data) {
    commit('SET_TOKEN', data.token);
    commit('SET_USER', { username: data.username, role: data.role });
    commit('SET_MENU', data.menu);
  },
  async fetchMe({ commit }) {
    const { data } = await authApi.getMe();
    commit('SET_USER', { username: data.username, role: data.role });
    commit('SET_MENU', data.menu);
  },
  logout({ commit }) {
    commit('LOGOUT');
  },
  fetchPrescription({ commit }, id) {
    // Fetch prescription data from API and commit to state
  },
  savePrescription({ commit }, prescription) {
    // Save prescription data to API
  },
  uploadPrescriptionImages({ commit }, images) {
    // Upload prescription images to API
  },
  fetchDrugImages({ commit }, drugId) {
    // Fetch drug images from API and commit to state
  }
};

const getters = {
  isLoggedIn: (state) => !!state.token,
  user: (state) => state.user,
  menu: (state) => state.menu,
  currentPrescription: (state) => state.currentPrescription,
  prescriptionImages: (state) => state.prescriptionImages,
  drugImages: (state) => state.drugImages
};

export default createStore({
  state,
  mutations,
  actions,
  getters
});
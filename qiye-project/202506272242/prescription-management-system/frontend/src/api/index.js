import axios from "axios";

// 响应拦截器：401自动跳转登录
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

const apiClient = axios.create({
  // baseURL: 'http://localhost:8001/api', // development
  baseURL: "/api", //produce

  headers: {
    "Content-Type": "application/json",
  },
});

// 给 apiClient 添加请求拦截器，自动带上token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Prescription API
export const prescriptionApi = {
  addPrescription(data) {
    return apiClient.post("/prescriptions", data);
  },
  getPrescription(id) {
    return apiClient.get(`/prescriptions/${id}`);
  },
  listPrescriptions(params) {
    return apiClient.get("/prescriptions", { params });
  },
  updatePrescription(id, data) {
    return apiClient.put(`/prescriptions/${id}`, data);
  },
  deletePrescription(id) {
    // return apiClient.delete(`/prescriptions/${id}`);

    return apiClient.delete("/prescriptions", { data: [id] });
  },
  uploadPrescriptionImage(id, formData) {
    return apiClient.post(`/prescriptions/${id}/images`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },
};

// Drug API
export const drugApi = {
  createDrug(data) {
    return apiClient.post("/drugs", data);
  },
  getDrugDetail(id) {
    return apiClient.get(`/drugs/${id}`);
  },
  getDrugs(params) {
    return apiClient.get("/drugs", { params });
  },
  listDrugs(params) {
    return apiClient.get("/drugs", { params });
  },
  updateDrug(id, data) {
    return apiClient.put(`/drugs/${id}`, data);
  },
  deleteDrug(id) {
    return apiClient.delete(`/drugs/${id}`);
  },
  uploadDrugImage(id, formData) {
    return apiClient.post(`/drugs/${id}/images`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },
  addDrug(data) {
    return apiClient.post("/drugs", data);
  },
  addDrugWithImages(formData) {
    return apiClient.post("/drugs/with-images", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },
};

// Auth API
export const authApi = {
  login(data) {
    return apiClient.post("/auth/login", data);
  },
  register(data) {
    return apiClient.post("/auth/register", data);
  },
  getMe() {
    return apiClient.get("/auth/me");
  },
  refreshToken() {
    return apiClient.post("/auth/refresh-token");
  },
};

// User API
export const userApi = {
  getUsers(params) {
    return apiClient.get("/auth/users", { params });
  },
  // 你可以继续扩展如新增、编辑、删除用户等API
};

export default apiClient;

import axios from './axios'

export const login = data => axios.post('/users/login', data)
export const register = data => axios.post('/users/register', data)
export const getUserProfile = id => axios.get(`/users/${id}`)
export const updateUserProfile = (id, data) => axios.put(`/users/${id}`, data)

// 获取用户头像URL
export const getUserAvatarUrl = (id) => {
  if (!id || id === 'undefined') {
    // 返回默认头像URL
    return '/api/users/default/avatar'
  }
  return `/api/users/${id}/avatar`
}

// 上传用户头像
export const uploadUserAvatar = (id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post(`/users/${id}/avatar`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

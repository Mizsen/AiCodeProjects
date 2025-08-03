import axios from './axios'

export const getPosts = (page = 1, size = 10, userId = '') =>
  axios.get('/posts', { params: { page, size, userId } })

export const searchPosts = (keyword = '', tag = '', page = 1, size = 10) =>
  axios.get('/posts/search', { params: { keyword, tag, page, size } })

export const getPost = id => axios.get(`/posts/${id}`)

export const createPost = data => axios.post('/posts', data)

export const updatePost = (id, data) => axios.put(`/posts/${id}`, data)

export const deletePost = id => axios.delete(`/posts/${id}`)


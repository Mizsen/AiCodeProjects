import axios from './axios'

export const likePost = (userId, postId) => axios.post(`/likes/post/${postId}/user/${userId}`)
export const unlikePost = (userId, postId) => axios.delete(`/likes/post/${postId}/user/${userId}`)
export const countLikes = postId => axios.get(`/likes/post/${postId}/count`)

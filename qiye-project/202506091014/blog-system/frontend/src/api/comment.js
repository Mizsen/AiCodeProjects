import axios from './axios'

export const addComment = data => axios.post('/comments', data)
export const getComments = postId => axios.get(`/comments/post/${postId}`)
export const deleteComment = id => axios.delete(`/comments/${id}`)

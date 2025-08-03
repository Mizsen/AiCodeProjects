import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useUserStore = defineStore('user', () => {
  // state
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // actions
  function setToken(val) {
    token.value = val
    localStorage.setItem('token', val)
  }

  function updateUserInfo(newInfo) {
    userInfo.value = newInfo
  }

  async function login(username, password) {
    try {
      const response = await axios.post('/api/users/login', { username, password })
      setToken(response.data.token)
      await fetchUserInfo()
      return true
    } catch (error) {
      throw error
    }
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const response = await axios.get('/api/users/me', {
        headers: { Authorization: `Bearer ${token.value}` },
      })
      userInfo.value = response.data
      console.log("fetchUserInfo",response.data)
    } catch (error) {
      logout()
    }
  }

  function logout() {
    userInfo.value = null
    setToken('')
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, login, fetchUserInfo, logout, updateUserInfo }
})

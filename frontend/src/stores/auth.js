import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../services/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!token.value)

  async function login(username, password) {
    const response = await api.post('/api/auth/login', { username, password })
    if (response.data.success) {
      const data = response.data.data
      token.value = data.token
      user.value = {
        id: data.userId,
        username: data.username,
        nickname: data.nickname,
        email: data.email
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(user.value))
    }
    return response.data
  }

  async function register(userData) {
    const response = await api.post('/api/auth/register', userData)
    return response.data
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const response = await api.get('/api/users/me')
      if (response.data.success) {
        user.value = response.data.data
        localStorage.setItem('user', JSON.stringify(user.value))
      }
    } catch (error) {
      logout()
    }
  }

  async function updateUser(data) {
    const response = await api.put('/api/users/me', data)
    if (response.data.success) {
      user.value = { ...user.value, ...data }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
    return response.data
  }

  async function changePassword(oldPassword, newPassword) {
    const response = await api.put('/api/users/me/password', { oldPassword, newPassword })
    return response.data
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    fetchUser,
    updateUser,
    changePassword
  }
})

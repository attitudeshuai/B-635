import axios from 'axios'
import { useToastStore } from '../stores/toast'

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => response,
  error => {
    const toast = useToastStore()
    
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        toast.error('登录已过期，请重新登录')
      } else if (status === 403) {
        toast.error('没有权限执行此操作')
      } else if (status === 404) {
        toast.error('请求的资源不存在')
      } else if (status >= 500) {
        toast.error('服务器错误，请稍后重试')
      } else if (data && data.message) {
        toast.error(data.message)
      }
    } else if (error.request) {
      toast.error('网络连接失败，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

export default api

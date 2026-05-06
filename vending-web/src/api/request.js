import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import { refreshTokenApi } from './user'

let isRefreshing = false
let refreshSubscribers = []
let isRedirecting = false
let messageShown = false

function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach(callback => callback(newToken))
  refreshSubscribers = []
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken') || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      if (res.code === 401) {
        handleUnauthorized()
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message))
    }
  },
  async error => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise(resolve => {
          subscribeTokenRefresh(newToken => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            resolve(request(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        handleUnauthorized()
        isRefreshing = false
        return Promise.reject(error)
      }

      try {
        const res = await refreshTokenApi({ refreshToken })
        const newToken = res.data.accessToken

        const userStore = useUserStore()
        userStore.setAccessToken(newToken)

        onTokenRefreshed(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return request(originalRequest)
      } catch (refreshError) {
        handleUnauthorized()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response?.status === 401) {
      handleUnauthorized()
    } else {
      const currentPath = window.location.pathname
      if (currentPath !== '/login' && !error.config._retry) {
        ElMessage.error(error.message || '网络错误')
      }
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  const currentPath = window.location.pathname
  if (currentPath === '/login') return

  if (!isRedirecting) {
    isRedirecting = true
    if (!messageShown) {
      messageShown = true
      ElMessage.error('登录已过期，请重新登录')
    }

    const userStore = useUserStore()
    userStore.logout()
    router.push('/login')

    setTimeout(() => {
      isRedirecting = false
      messageShown = false
    }, 1000)
  }
}

export default request

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import { refreshTokenApi } from './user'

let isRefreshing = false
let refreshSubscribers = []

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
    const userStore = useUserStore()
    if (userStore.accessToken) {
      config.headers.Authorization = `Bearer ${userStore.accessToken}`
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
      if (res.code !== 401) {
        ElMessage.error(res.message || '请求失败')
      }
      if (res.code === 401) {
        const currentPath = window.location.pathname
        if (currentPath !== '/login') {
          const userStore = useUserStore()
          userStore.forceLogout()
          router.push('/login')
        }
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

      const userStore = useUserStore()
      const refreshToken = userStore.refreshToken

      if (!refreshToken) {
        const currentPath = window.location.pathname
        if (currentPath !== '/login') {
          userStore.forceLogout()
          router.push('/login')
        }
        isRefreshing = false
        return Promise.reject(error)
      }

      try {
        const res = await refreshTokenApi({ refreshToken })
        const newToken = res.data.accessToken

        userStore.setAccessToken(newToken)

        onTokenRefreshed(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return request(originalRequest)
      } catch (refreshError) {
        const currentPath = window.location.pathname
        if (currentPath !== '/login') {
          userStore.forceLogout()
          router.push('/login')
        }
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response?.status === 401) {
      const currentPath = window.location.pathname
      if (currentPath !== '/login') {
        const userStore = useUserStore()
        userStore.forceLogout()
        router.push('/login')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

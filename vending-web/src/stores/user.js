import { defineStore } from 'pinia'
import { ref } from 'vue'
import { logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 兼容旧的 token 字段
  const token = accessToken

  function setAccessToken(newToken) {
    accessToken.value = newToken
    localStorage.setItem('accessToken', newToken)
    localStorage.setItem('token', newToken) // 兼容旧代码
  }

  function setRefreshToken(newToken) {
    refreshToken.value = newToken
    localStorage.setItem('refreshToken', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  async function logout() {
    try {
      await logoutApi({ refreshToken: refreshToken.value })
    } catch (e) {
      // 忽略错误，继续清理本地状态
    }
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { accessToken, refreshToken, token, userInfo, setAccessToken, setRefreshToken, setUserInfo, logout }
})
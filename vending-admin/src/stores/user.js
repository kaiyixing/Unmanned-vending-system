import { defineStore } from 'pinia'
import { ref } from 'vue'
import { logoutApi } from '@/api/user'

let isLoggingOut = false

export const useUserStore = defineStore('adminUser', () => {
  const accessToken = ref(localStorage.getItem('adminAccessToken') || '')
  const refreshToken = ref(localStorage.getItem('adminRefreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('adminUserInfo') || '{}'))

  const token = accessToken

  function setAccessToken(newToken) {
    accessToken.value = newToken
    localStorage.setItem('adminAccessToken', newToken)
  }

  function setRefreshToken(newToken) {
    refreshToken.value = newToken
    localStorage.setItem('adminRefreshToken', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('adminUserInfo', JSON.stringify(info))
  }

  function clearLocalState() {
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = {}
    localStorage.removeItem('adminAccessToken')
    localStorage.removeItem('adminRefreshToken')
    localStorage.removeItem('adminUserInfo')
  }

  async function logout() {
    if (isLoggingOut) return
    isLoggingOut = true
    try {
      await logoutApi({ refreshToken: refreshToken.value })
    } catch (e) {
    } finally {
      clearLocalState()
      isLoggingOut = false
    }
  }

  function forceLogout() {
    clearLocalState()
  }

  return { accessToken, refreshToken, token, userInfo, setAccessToken, setRefreshToken, setUserInfo, logout, forceLogout }
})

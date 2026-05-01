import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('adminUser', () => {
  const token = ref(localStorage.getItem('adminToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('adminUserInfo') || '{}'))

  function setToken(t) {
    token.value = t
    localStorage.setItem('adminToken', t)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('adminUserInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminUserInfo')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
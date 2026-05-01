<template>
  <div class="login-page">
    <div class="login-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    <div class="login-container">
      <div class="login-card clay-box">
        <div class="login-header">
          <h1>智能货柜</h1>
          <p>线上选购，自助取货</p>
        </div>
        <el-tabs v-model="activeTab" class="auth-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" @submit.prevent="handleLogin">
              <div class="mb-3">
                <input class="form-clay" v-model="loginForm.username" placeholder="请输入用户名" />
              </div>
              <div class="mb-4">
                <input class="form-clay" v-model="loginForm.password" type="password" placeholder="请输入密码" />
              </div>
              <button class="btn-clay btn-primary w-100" type="submit">登录</button>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" @submit.prevent="handleRegister">
              <div class="mb-3">
                <input class="form-clay" v-model="registerForm.username" placeholder="请输入用户名" />
              </div>
              <div class="mb-3">
                <input class="form-clay" v-model="registerForm.phone" placeholder="请输入手机号" />
              </div>
              <div class="mb-3">
                <input class="form-clay" v-model="registerForm.password" type="password" placeholder="请设置密码" />
              </div>
              <div class="mb-4">
                <input class="form-clay" v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" />
              </div>
              <button class="btn-clay btn-primary w-100" type="submit">注册</button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { loginApi, registerApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerForm = reactive({
  username: '', phone: '', password: '', confirmPassword: ''
})
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  try {
    const res = await loginApi(loginForm)
    if (res.data && res.data.token) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      ElMessage.success('登录成功')
      window.location.href = '/'
    } else {
      ElMessage.error('登录失败：未获取到有效响应')
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  }
}

async function handleRegister() {
  try {
    await registerFormRef.value.validate()
    await registerApi(registerForm)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = ''
  } catch (e) {
    if (e.message) {
      ElMessage.error(e.message)
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-blobs {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.blob {
  position: absolute;
  opacity: 0.7;
  animation: float 20s ease-in-out infinite;
}

.blob-1 {
  width: 300px;
  height: 300px;
  background-color: var(--color-primary);
  top: 10%;
  left: 5%;
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background-color: var(--color-secondary);
  bottom: 5%;
  right: 10%;
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
  animation-delay: -5s;
}

.blob-3 {
  width: 250px;
  height: 250px;
  background-color: var(--color-accent);
  top: 20%;
  right: 25%;
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
  animation-delay: -10s;
}

@keyframes float {
  0% { transform: translateY(0px) rotate(-2deg); }
  50% { transform: translateY(-35px) rotate(2deg); }
  100% { transform: translateY(0px) rotate(-2deg); }
}

.login-container {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 20px;
}

.login-card {
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.login-header p {
  color: #8a7e74;
  font-size: 0.95rem;
}

.auth-tabs :deep(.el-tabs__header) {
  background-color: var(--color-background);
  border-radius: 20px;
  padding: 4px;
  box-shadow: var(--shadow-clay-inset);
  border: none;
  margin-bottom: 24px;
}

.auth-tabs :deep(.el-tabs__nav) {
  border: none;
  width: 100%;
  display: flex;
}

.auth-tabs :deep(.el-tabs__item) {
  flex: 1;
  border: none;
  font-weight: 700;
  font-size: 1rem;
  padding: 10px 0;
  color: var(--color-text);
}

.auth-tabs :deep(.el-tabs__item.is-active) {
  background-color: var(--color-white);
  box-shadow: var(--shadow-clay);
  border-radius: 16px;
  color: var(--color-primary);
}

.auth-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.w-100 { width: 100%; }

@media (max-width: 480px) {
  .login-container { width: 100%; }
  .login-card { padding: 24px; }
}
</style>
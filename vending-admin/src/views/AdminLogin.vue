<template>
  <div class="admin-login-page">
    <div class="login-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    <div class="login-container">
      <div class="login-card clay-box">
        <div class="login-header">
          <h1>智能货柜</h1>
          <p>管理后台</p>
        </div>
        <el-form :model="form" :rules="rules" ref="formRef">
          <div class="mb-3">
            <input class="form-clay" v-model="form.username" placeholder="请输入用户名" />
          </div>
          <div class="mb-4">
            <input class="form-clay" v-model="form.password" type="password" placeholder="请输入密码" />
          </div>
          <button class="btn-clay btn-primary w-100" type="button" @click="handleLogin" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </el-form>
        <div class="back-link">
          <a href="/" @click.prevent="goHome">返回用户端</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await loginApi(form)
    if (res.data && res.data.role < 1) {
      ElMessage.error('无权访问管理后台')
      return
    }
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    window.location.href = '/'
  } catch (e) {
    if (e.message) {
      ElMessage.error(e.message)
    }
  } finally {
    loading.value = false
  }
}

function goHome() {
  window.location.href = '/'
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background-color: var(--admin-bg, #F4F1EE);
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
  background-color: var(--admin-primary, #A2C2E8);
  top: 10%;
  left: 5%;
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background-color: #FFCBA4;
  bottom: 5%;
  right: 10%;
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
  animation-delay: -5s;
}

.blob-3 {
  width: 250px;
  height: 250px;
  background-color: #BEE8D2;
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
  background: var(--admin-bg, #F4F1EE);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 1.8rem;
  margin-bottom: 8px;
  color: var(--admin-text, #4D423C);
}

.login-header p {
  color: #8a7e74;
  font-size: 0.95rem;
}

.w-100 { width: 100%; }

.form-clay {
  width: 100%;
  padding: 14px 18px;
  background-color: var(--admin-bg, #F4F1EE);
  border: none;
  border-radius: 20px;
  box-shadow: inset 3px 3px 5px rgba(209, 204, 199, 0.5), inset -3px -3px 5px #FFFFFF;
  color: var(--admin-text, #4D423C);
  font-size: 1rem;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  transition: all 0.3s ease-in-out;
}

.form-clay:focus {
  outline: none;
  box-shadow: inset 5px 5px 8px rgba(209, 204, 199, 0.7), inset -5px -5px 8px #FFFFFF;
}

.form-clay::placeholder {
  color: #a8a098;
}

.btn-clay {
  padding: 12px 30px;
  font-weight: 700;
  font-size: 1rem;
  color: var(--admin-text, #4D423C);
  background-color: var(--admin-bg, #F4F1EE);
  border: none;
  border-radius: 20px;
  box-shadow: 5px 5px 15px #d1ccc7, -5px -5px 15px #FFFFFF;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-clay:hover:not(:disabled) {
  transform: scale(0.97);
  box-shadow: 10px 10px 20px #d1ccc7, -10px -10px 20px #FFFFFF;
}

.btn-clay.btn-primary {
  background-color: var(--admin-primary, #A2C2E8);
  box-shadow: 5px 5px 15px var(--admin-primary-dark, #8eb0d3), -5px -5px 15px var(--admin-primary-light, #b6d4fb);
  color: #FFFFFF;
}

.btn-clay:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.back-link {
  text-align: center;
  margin-top: 20px;
}

.back-link a {
  color: #8a7e74;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s;
}

.back-link a:hover {
  color: var(--admin-primary, #A2C2E8);
}

@media (max-width: 480px) {
  .login-container { width: 100%; }
  .login-card { padding: 24px; }
}
</style>

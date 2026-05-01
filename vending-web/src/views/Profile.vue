<template>
  <div class="profile-page">
    <NavBar />
    <div class="page-content">
      <div class="container" v-loading="loading">
        <h2 class="mb-4">个人中心</h2>

        <div class="profile-card clay-box mb-4">
          <div class="avatar-row">
            <div class="avatar-circle">
              <el-icon :size="48"><User /></el-icon>
            </div>
            <div class="user-info">
              <h3>{{ userInfo.username || '用户' }}</h3>
              <p class="text-muted">{{ userInfo.phone || '未绑定手机号' }}</p>
            </div>
          </div>
        </div>

        <div class="form-section clay-box">
          <h4 class="mb-4">编辑信息</h4>
          <el-form :model="form" label-width="80px">
            <el-form-item label="用户名">
              <span>{{ userInfo.username }}</span>
            </el-form-item>
            <el-form-item label="手机号">
              <input class="form-clay" v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <input class="form-clay" v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="真实姓名">
              <input class="form-clay" v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item>
              <button class="btn-clay btn-primary" @click="handleSave" :disabled="saving">保存</button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo } from '@/api/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const userInfo = ref({})

const form = reactive({
  phone: '',
  email: '',
  realName: ''
})

async function fetchInfo() {
  loading.value = true
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
    form.phone = userInfo.value.phone || ''
    form.email = userInfo.value.email || ''
    form.realName = userInfo.value.realName || ''
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateUserInfo(form)
    ElMessage.success('保存成功')
    fetchInfo()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchInfo)
</script>

<style scoped>
.page-content { padding-top: 80px; min-height: 100vh; }

.profile-card { padding: 32px; }

.avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #e9f1fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.form-section { padding: 32px; }

.form-section :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-section :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--color-text);
}
</style>
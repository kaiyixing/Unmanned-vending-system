<template>
  <div class="profile-page">
    <NavBar />
    <div class="page-content">
      <div class="container" v-loading="loading">
        <h2 class="mb-4">个人中心</h2>

        <div class="profile-card clay-box mb-4">
          <div class="avatar-row">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleAvatarUpload"
            >
              <div class="avatar-circle">
                <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
                <el-icon v-else :size="48"><User /></el-icon>
                <div class="avatar-overlay">
                  <el-icon :size="24"><Camera /></el-icon>
                </div>
              </div>
            </el-upload>
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
import { User, Camera } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
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

function beforeUpload(file) {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/jpg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像大小不能超过 2MB!')
    return false
  }
  return true
}

async function handleAvatarUpload({ file }) {
  uploading.value = true
  try {
    const res = await uploadAvatar(file)
    const avatarUrl = res.data
    await updateUserInfo({
      avatar: avatarUrl
    })
    userInfo.value.avatar = avatarUrl
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
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

.avatar-uploader {
  cursor: pointer;
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
  overflow: hidden;
  position: relative;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  display: none;
  align-items: center;
  justify-content: center;
  color: white;
}

.avatar-circle:hover .avatar-overlay {
  display: flex;
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
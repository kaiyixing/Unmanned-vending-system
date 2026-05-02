<template>
  <div class="pickup-verify-page">
    <div class="verify-card">
      <div class="header-section">
        <div class="icon-wrapper">
          <el-icon :size="48" color="#fff"><Ticket /></el-icon>
        </div>
        <h2>取货验证</h2>
        <p>请输入6位取货码完成取货</p>
      </div>

      <div class="input-section">
        <div class="code-boxes">
          <div
            v-for="(n, i) in 6"
            :key="i"
            class="code-box"
            :class="{ active: i === currentIndex, filled: codeValue[i] }"
          >
            {{ codeValue[i] || '' }}
          </div>
        </div>
        <input
          ref="hiddenInput"
          v-model="codeValue"
          type="text"
          maxlength="6"
          class="hidden-input"
          @input="handleInput"
          @keyup.enter="handleVerify"
        />
      </div>

      <el-button
        type="primary"
        size="large"
        :loading="loading"
        :disabled="codeValue.length !== 6"
        @click="handleVerify"
        class="verify-btn"
      >
        <span v-if="!loading">验证并取货</span>
        <span v-else>验证中...</span>
      </el-button>

      <div v-if="success" class="success-overlay">
        <div class="success-content">
          <el-icon :size="80" color="#27ae60"><CircleCheck /></el-icon>
          <h3>取货成功！</h3>
          <p>商品已取出，感谢您的使用</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Ticket } from '@element-plus/icons-vue'
import { verifyPickupCode } from '@/api/pickup'

const codeValue = ref('')
const loading = ref(false)
const success = ref(false)
const currentIndex = ref(0)
const hiddenInput = ref(null)

onMounted(() => {
  hiddenInput.value.focus()
})

function handleInput() {
  codeValue.value = codeValue.value.replace(/\D/g, '')
  currentIndex.value = Math.min(codeValue.value.length, 5)
}

async function handleVerify() {
  if (codeValue.value.length !== 6) {
    ElMessage.warning('请输入完整的6位取货码')
    return
  }

  loading.value = true
  success.value = false

  try {
    await verifyPickupCode(codeValue.value)
    success.value = true
    codeValue.value = ''
    currentIndex.value = 0

    setTimeout(() => {
      success.value = false
      nextTick(() => {
        hiddenInput.value.focus()
      })
    }, 2500)
  } catch (e) {
    ElMessage.error(e.message || '验证失败，请检查取货码是否正确')
    codeValue.value = ''
    currentIndex.value = 0
    nextTick(() => {
      hiddenInput.value.focus()
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pickup-verify-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  background: #f5f5f5;
}

.verify-card {
  position: relative;
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 56px 48px 48px;
  text-align: center;
  overflow: hidden;
}

.header-section {
  margin-bottom: 40px;
}

.icon-wrapper {
  width: 88px;
  height: 88px;
  background: #A2C2E8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 4px 16px rgba(162, 194, 232, 0.4);
}

.header-section h2 {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--admin-dark, #333);
  margin: 0 0 8px;
}

.header-section p {
  font-size: 1rem;
  color: #8a7e74;
  margin: 0;
}

.input-section {
  position: relative;
  margin-bottom: 40px;
}

.code-boxes {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.code-box {
  width: 56px;
  height: 72px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--admin-dark, #333);
  background: #f8f9fa;
  transition: all 0.2s ease;
}

.code-box.active {
  border-color: #A2C2E8;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(162, 194, 232, 0.2);
  transform: scale(1.05);
}

.code-box.filled {
  border-color: #A2C2E8;
  background: #fff;
}

.hidden-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.verify-btn {
  width: 100%;
  height: 56px;
  font-size: 1.1rem;
  font-weight: 700;
  border-radius: 12px;
  background: #A2C2E8;
  border: none;
  color: #fff;
  box-shadow: 0 4px 12px rgba(162, 194, 232, 0.4);
  transition: all 0.2s ease;
}

.verify-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(162, 194, 232, 0.5);
  background: #8bb8e0;
}

.verify-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: slideIn 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  z-index: 10;
}

.success-content {
  text-align: center;
}

.success-content h3 {
  font-size: 1.8rem;
  font-weight: 800;
  color: #27ae60;
  margin: 16px 0 8px;
}

.success-content p {
  font-size: 1rem;
  color: #8a7e74;
  margin: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

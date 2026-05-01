<template>
  <div class="pickup-page">
    <NavBar />
    <div class="page-content">
      <div class="container text-center" v-loading="loading">
        <h2 class="mb-4">取货码</h2>

        <div class="pickup-card clay-box" v-if="pickupCode">
          <div class="code-display">{{ pickupCode.codeValue }}</div>
          <p class="text-muted">请在有效期内到指定货柜取货</p>
          <div class="countdown" v-if="!expired">
            <el-icon :size="20"><Clock /></el-icon>
            <span>剩余时间：{{ countdownText }}</span>
          </div>
          <div v-else class="expired-tag">已过期</div>
        </div>

        <div class="action-btns mt-4">
          <button class="btn-clay btn-primary" @click="$router.push('/orders')">查看订单</button>
          <button class="btn-clay" @click="$router.push('/')">返回首页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { getPickupCode } from '@/api/pickup'

const route = useRoute()
const pickupCode = ref(null)
const loading = ref(false)
const timer = ref(null)
const now = ref(new Date())

const countdownText = computed(() => {
  if (!pickupCode.value) return ''
  const expire = new Date(pickupCode.value.expireTime)
  const diff = expire - now.value
  if (diff <= 0) return '已过期'
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  return `${h}小时${m}分${s}秒`
})

const expired = computed(() => {
  if (!pickupCode.value) return false
  return new Date(pickupCode.value.expireTime) <= now.value
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getPickupCode(route.params.orderId)
    pickupCode.value = res.data
  } catch (e) {
    ElMessage.error('获取取货码失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  timer.value = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
</script>

<style scoped>
.page-content {
  padding-top: 80px;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.pickup-card {
  padding: 48px 32px;
  max-width: 400px;
  margin: 0 auto;
}

.code-display {
  font-size: 3.5rem;
  font-weight: 800;
  letter-spacing: 8px;
  color: var(--color-primary);
  padding: 20px;
  background: #e9f1fa;
  border-radius: var(--border-radius-soft);
  margin-bottom: 16px;
}

.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  font-weight: 600;
  color: var(--color-secondary);
}

.expired-tag {
  color: #c0392b;
  font-weight: 700;
  font-size: 1.2rem;
  margin-top: 16px;
}

.action-btns {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}
</style>
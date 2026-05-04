<template>
  <div class="checkout-page">
    <NavBar />
    <div class="page-content">
      <div class="container" v-loading="loading">
        <button class="btn-clay btn-sm mb-4" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </button>

        <h2 class="mb-4">确认订单</h2>

        <!-- Order Items -->
        <div class="order-items clay-box mb-4">
          <div class="order-item" v-for="item in cartStore.items" :key="item.productId">
            <div class="oi-name">{{ item.name }} x{{ item.quantity }}</div>
            <div class="oi-price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
          <div class="order-total">
            <span>合计</span>
            <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Cabinet Info -->
        <div class="cabinet-info-box clay-box mb-4">
          <h4><el-icon><Location /></el-icon> 取货货柜</h4>
          <p class="text-muted">{{ cartStore.cabinetName }}</p>
        </div>

        <!-- 第一步：选择支付方式 -->
        <div v-if="step === 1" class="pay-channel clay-box mb-4">
          <h4>选择支付方式</h4>
          <div class="channel-options">
            <div
              v-for="ch in channels"
              :key="ch.value"
              class="channel-option"
              :class="{ active: payChannel === ch.value }"
              @click="payChannel = ch.value"
            >
              <el-icon :size="28"><component :is="ch.icon" /></el-icon>
              <span>{{ ch.label }}</span>
            </div>
          </div>
          <button class="btn-clay btn-primary btn-lg w-100 mt-4" @click="nextStep">
            下一步 ¥{{ cartStore.totalPrice.toFixed(2) }}
          </button>
        </div>

        <!-- 第二步：展示收款码（如果选了微信/支付宝） -->
        <div v-if="step === 2" class="pay-qrcode clay-box mb-4">
          <h4>{{ currentChannel.label }} 扫码支付</h4>
          <p class="text-muted mb-3">请扫描下方二维码支付，支付完成后点击「确认付款」</p>
          
          <div class="qrcode-container text-center py-4">
            <!-- 收款码图片占位，实际使用时替换为你自己的收款码图片 -->
            <div v-if="payChannel === 'wechat'" class="qrcode-box wechat">
              <div class="qrcode-placeholder">
                <span class="icon">💚</span>
                <p>微信收款码</p>
                <p class="hint">请将你的微信收款码放到 public 目录，命名为 wechat.png</p>
              </div>
            </div>
            <div v-else-if="payChannel === 'alipay'" class="qrcode-box alipay">
              <div class="qrcode-placeholder">
                <span class="icon">💙</span>
                <p>支付宝收款码</p>
                <p class="hint">请将你的支付宝收款码放到 public 目录，命名为 alipay.png</p>
              </div>
            </div>
          </div>

          <div class="amount-display">
            <span class="amount-label">支付金额</span>
            <span class="amount-value">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
          </div>

          <div class="action-buttons">
            <button class="btn-clay btn-sm" @click="step = 1">返回选择</button>
            <button class="btn-clay btn-primary btn-lg" @click="handleConfirmPaid">
              我已付款，确认取货
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, ChatDotRound, Wallet, Money } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { payOrder } from '@/api/payment'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const step = ref(1)
const payChannel = ref('mock')

const channels = [
  { value: 'wechat', label: '微信支付', icon: ChatDotRound },
  { value: 'alipay', label: '支付宝', icon: Wallet },
  { value: 'mock', label: '模拟支付', icon: Money }
]

const currentChannel = computed(() => channels.find(ch => ch.value === payChannel.value))

const nextStep = async () => {
  if (!payChannel.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  if (payChannel.value === 'mock') {
    // 模拟支付直接确认
    await confirmMockPay()
  } else {
    // 微信/支付宝显示收款码
    step.value = 2
  }
}

const confirmMockPay = async () => {
  try {
    await ElMessageBox.confirm('确认模拟支付吗？', '提示', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
    await handlePay()
  } catch (e) {
    if (e !== 'cancel') throw e
  }
}

const handleConfirmPaid = async () => {
  try {
    await ElMessageBox.confirm(
      '确认已完成付款吗？确认后将立即出出货。',
      '付款确认',
      {
        confirmButtonText: '已付款，出出货',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await handlePay()
  } catch (e) {
    if (e !== 'cancel') throw e
  }
}

const handlePay = async () => {
  try {
    loading.value = true

    // 1. Create order
    const orderItems = cartStore.items.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))

    const cabinetIdValue = cartStore.cabinetId
    if (!cabinetIdValue) {
      ElMessage.error('购物车中缺少货柜信息，请重新选择商品')
      loading.value = false
      return
    }

    const orderRes = await createOrder({
      cabinetId: cabinetIdValue,
      items: orderItems
    })

    const orderId = orderRes.data.orderId

    // 2. Pay
    await payOrder(orderId, payChannel.value)

    ElMessage.success('支付成功')
    cartStore.clearCart()
    router.push(`/pickup-code/${orderId}`)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '支付失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-content { padding-top: 80px; min-height: 100vh; }

.order-items { padding: 20px; }

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #e0dbd6;
}

.order-item:last-of-type { border-bottom: none; }

.order-total {
  display: flex;
  justify-content: space-between;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 2px solid var(--color-primary);
  font-weight: 800;
  font-size: 1.1rem;
}

.total-price {
  color: var(--color-primary);
  font-size: 1.3rem;
}

.cabinet-info-box, .pay-channel, .pay-qrcode {
  padding: 20px;
}

.channel-options {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.channel-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-radius: var(--border-radius-soft);
  border: 2px solid transparent;
  cursor: pointer;
  transition: var(--transition-smooth);
}

.channel-option:hover {
  background: #f0ede9;
}

.channel-option.active {
  border-color: var(--color-primary);
  background: #e9f1fa;
  color: var(--color-primary);
}

.qrcode-container {
  border-radius: 12px;
  background: #f8f8f8;
  margin: 20px 0;
}

.qrcode-box {
  display: inline-block;
}

.qrcode-placeholder {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #ddd;
}

.qrcode-placeholder .icon {
  font-size: 3rem;
}

.qrcode-placeholder .hint {
  font-size: 0.75rem;
  color: #999;
  margin-top: 8px;
  padding: 0 20px;
}

.amount-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f0f8f5;
  border-radius: 12px;
  margin: 16px 0;
}

.amount-label {
  font-size: 0.95rem;
  color: #666;
}

.amount-value {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--color-primary);
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.w-100 { width: 100%; }
</style>

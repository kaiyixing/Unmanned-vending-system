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

        <!-- Pay Channel -->
        <div class="pay-channel clay-box mb-4">
          <h4>选择支付方式</h4>
          <div class="channel-options">
            <div
              v-for="ch in channels"
              :key="ch.value"
              class="channel-option"
              :class="{ active: payChannel === ch.value }"
              @click="payChannel = ch.value"
            >
              <el-icon :size="24"><component :is="ch.icon" /></el-icon>
              <span>{{ ch.label }}</span>
            </div>
          </div>
        </div>

        <button class="btn-clay btn-primary btn-lg w-100" @click="handlePay" :disabled="!payChannel">
          确认支付 ¥{{ cartStore.totalPrice.toFixed(2) }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Wallet, Money } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { payOrder } from '@/api/payment'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const payChannel = ref('mock')

const channels = [
  { value: 'wechat', label: '微信支付', icon: 'ChatDotRound' },
  { value: 'alipay', label: '支付宝', icon: 'Wallet' },
  { value: 'mock', label: '模拟支付', icon: 'Money' }
]

async function handlePay() {
  try {
    await ElMessageBox.confirm('确认支付吗？', '提示', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })

    loading.value = true

    // 1. Create order
    const orderItems = cartStore.items.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))

    const cabinetIdValue = cartStore.cabinetId.value
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

.cabinet-info-box, .pay-channel {
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
  gap: 8px;
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

.w-100 { width: 100%; }
</style>
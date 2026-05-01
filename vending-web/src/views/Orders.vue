<template>
  <div class="orders-page">
    <NavBar />
    <div class="page-content">
      <div class="container" v-loading="loading">
        <h2 class="mb-4">我的订单</h2>

        <!-- Status Filter -->
        <div class="status-bar mb-4">
          <button
            v-for="s in statusOptions"
            :key="s.value"
            class="btn-clay btn-sm"
            :class="{ 'btn-primary': statusFilter === s.value }"
            @click="statusFilter = s.value; fetchOrders()"
          >
            {{ s.label }}
          </button>
        </div>

        <!-- Order List -->
        <div class="order-list" v-if="orders.length > 0">
          <div class="order-card clay-box" v-for="order in orders" :key="order.orderId">
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span :class="['badge-clay', getStatusClass(order.status)]">{{ getStatusText(order.status) }}</span>
            </div>
            <div class="order-items">
              <div class="oi" v-for="item in order.items" :key="item.productId">
                <span>{{ item.productName }} x{{ item.quantity }}</span>
                <span>¥{{ item.unitPrice }}</span>
              </div>
            </div>
            <div class="order-footer">
              <span class="order-total">合计：¥{{ order.totalAmount }}</span>
              <div class="order-actions">
                <button v-if="order.status === 0" class="btn-clay btn-sm btn-primary" @click="goPay(order)">去支付</button>
                <button v-if="order.status === 0" class="btn-clay btn-sm" @click="cancelOrderHandler(order)">取消</button>
                <button v-if="order.status === 1" class="btn-clay btn-sm" @click="$router.push(`/pickup-code/${order.orderId}`)">取货码</button>
                <button v-if="order.status === 1" class="btn-clay btn-sm" style="color:#c0392b" @click="applyRefund(order)">申请退款</button>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { getUserOrders, cancelOrder, applyRefund as applyRefundApi } from '@/api/order'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const statusFilter = ref(-1)

const statusOptions = [
  { value: -1, label: '全部' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' },
  { value: 5, label: '已退款' }
]

const statusMap = {
  0: { text: '待支付', cls: 'badge-warning' },
  1: { text: '已支付', cls: 'badge-info' },
  2: { text: '已完成', cls: 'badge-success' },
  3: { text: '已取消', cls: 'badge-danger' },
  4: { text: '退款中', cls: 'badge-warning' },
  5: { text: '已退款', cls: 'badge-danger' }
}

function getStatusText(status) {
  return statusMap[status]?.text || '未知'
}

function getStatusClass(status) {
  return statusMap[status]?.cls || 'badge-info'
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = statusFilter.value >= 0 ? { status: statusFilter.value } : {}
    const res = await getUserOrders(params)
    orders.value = res.data || []
  } catch (e) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

function goPay(order) {
  router.push(`/pickup-code/${order.orderId}`)
}

async function cancelOrderHandler(order) {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示')
    await cancelOrder(order.orderId)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {}
}

async function applyRefund(order) {
  try {
    const { value } = await ElMessageBox.prompt('请输入退款原因', '申请退款', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '退款原因不能为空'
    })
    await applyRefundApi(order.orderId, { reason: value })
    ElMessage.success('退款申请已提交')
    fetchOrders()
  } catch (e) {}
}

onMounted(fetchOrders)
</script>

<style scoped>
.page-content { padding-top: 80px; min-height: 100vh; }

.status-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e0dbd6;
  margin-bottom: 12px;
}

.order-no {
  font-size: 0.85rem;
  color: #8a7e74;
}

.order-items { margin-bottom: 12px; }

.oi {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 0.95rem;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e0dbd6;
}

.order-total {
  font-weight: 800;
  font-size: 1.1rem;
  color: var(--color-primary);
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
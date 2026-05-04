<template>
  <div class="order-manage">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-tag v-if="todayOnly" type="warning" closable @close="todayOnly = false; fetchData()">
          仅显示今日订单
        </el-tag>
        <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 150px" @change="fetchData">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="orderId" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="用户" width="120">
        <template #default="{ row }">
          {{ getUserName(row.userId) }}
        </template>
      </el-table-column>
      <el-table-column label="货柜" min-width="150">
        <template #default="{ row }">
          {{ getCabinetName(row.cabinetId) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payChannel" label="支付渠道" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 4" text type="success" @click="handleAudit(row, true)">同意</el-button>
            <el-button v-if="row.status === 4" text type="danger" @click="handleAudit(row, false)">拒绝</el-button>
          </div>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无订单数据" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      @current-change="fetchData"
    />

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="currentOrder">
        <p><strong>订单号：</strong>{{ currentOrder.orderNo }}</p>
        <p><strong>用户：</strong>{{ getUserName(currentOrder.userId) }}</p>
        <p><strong>货柜：</strong>{{ getCabinetName(currentOrder.cabinetId) }}</p>
        <p><strong>状态：</strong>{{ getStatusText(currentOrder.status) }}</p>
        <p><strong>金额：</strong>¥{{ currentOrder.totalAmount }}</p>
        <p><strong>支付渠道：</strong>{{ currentOrder.payChannel || '-' }}</p>
        <p><strong>创建时间：</strong>{{ currentOrder.createTime }}</p>
        <p><strong>支付时间：</strong>{{ currentOrder.payTime || '-' }}</p>
        <div v-if="currentRefund" style="margin-top: 16px; padding: 12px; background: var(--admin-bg); border-radius: 12px;">
          <p><strong>退款信息：</strong></p>
          <p>退款原因：{{ currentRefund.reason }}</p>
          <p>退款金额：¥{{ currentRefund.amount }}</p>
          <p>状态：{{ getRefundStatusText(currentRefund.status) }}</p>
          <p v-if="currentRefund.auditRemark">审核备注：{{ currentRefund.auditRemark }}</p>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="auditVisible" :title="auditApproved ? '同意退款' : '拒绝退款'" width="500px">
      <el-form>
        <el-form-item label="审核备注">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminOrderList, getRefundList, auditRefund } from '@/api/order'
import { adminCabinetList } from '@/api/cabinet'
import { userList } from '@/api/user'

const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const todayOnly = ref(false)
const detailVisible = ref(false)
const auditVisible = ref(false)
const currentOrder = ref(null)
const currentRefund = ref(null)
const auditApproved = ref(false)
const auditRemark = ref('')
const refundList = ref([])
const cabinets = ref([])
const users = ref([])

const statusOptions = [
  { value: null, label: '全部' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' },
  { value: 4, label: '退款中' },
  { value: 5, label: '已退款' }
]

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: '' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已取消', type: 'danger' },
  4: { text: '退款中', type: 'warning' },
  5: { text: '已退款', type: 'info' }
}

const refundStatusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已同意', type: 'success' },
  2: { text: '已拒绝', type: 'danger' }
}

function getStatusText(status) {
  return statusMap[status]?.text || '未知'
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

function getRefundStatusText(status) {
  return refundStatusMap[status]?.text || '未知'
}

function getUserName(userId) {
  const user = users.value.find(u => u.userId === userId)
  return user ? user.username : `用户#${userId}`
}

function getCabinetName(cabinetId) {
  const cabinet = cabinets.value.find(c => c.cabinetId === cabinetId)
  return cabinet ? `${cabinet.name} (${cabinet.city})` : `货柜#${cabinetId}`
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value !== null) params.status = statusFilter.value
    if (todayOnly.value) params.todayOnly = true
    const res = await adminOrderList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    await fetchRefunds()
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function fetchRefunds() {
  try {
    const res = await getRefundList({ page: 1, size: 1000 })
    refundList.value = res.data.records || []
  } catch (e) {
  }
}

async function fetchCabinets() {
  try {
    const res = await adminCabinetList({ page: 1, size: 100 })
    cabinets.value = res.data.records || []
  } catch (e) {}
}

async function fetchUsers() {
  try {
    const res = await userList()
    users.value = res.data || []
  } catch (e) {}
}

function viewDetail(row) {
  currentOrder.value = row
  currentRefund.value = refundList.value.find(r => r.orderId === row.orderId) || null
  detailVisible.value = true
}

async function handleAudit(row, approved) {
  const refund = refundList.value.find(r => r.orderId === row.orderId)
  if (!refund) {
    ElMessage.error('未找到退款记录')
    return
  }

  currentOrder.value = row
  auditApproved.value = approved
  auditRemark.value = ''
  auditVisible.value = true
}

async function submitAudit() {
  const refund = refundList.value.find(r => r.orderId === currentOrder.value.orderId)
  if (!refund) {
    ElMessage.error('未找到退款记录')
    return
  }

  try {
    await auditRefund(refund.refundId, auditApproved.value, auditRemark.value)
    ElMessage.success(auditApproved.value ? '已同意退款' : '已拒绝退款')
    auditVisible.value = false
    await fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  if (route.query.type === 'today') {
    todayOnly.value = true
  }
  fetchCabinets()
  fetchUsers()
  fetchData()
})

watch(() => route.query, (newQuery) => {
  if (newQuery.type === 'today') {
    todayOnly.value = true
  } else {
    todayOnly.value = false
  }
  page.value = 1
  fetchData()
}, { immediate: true })
</script>

<style scoped>
.order-manage { display: flex; flex-direction: column; gap: 16px; }
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--admin-white);
  border-radius: 16px;
  box-shadow: var(--shadow-clay);
}
.toolbar-left { display: flex; align-items: center; gap: 12px; }

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>

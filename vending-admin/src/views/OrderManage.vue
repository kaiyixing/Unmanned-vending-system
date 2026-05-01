<template>
  <div class="order-manage">
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 150px" @change="fetchData">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="orderId" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="cabinetId" label="货柜ID" width="100" />
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
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.status === 4" text type="success" @click="handleAudit(row, true)">同意退款</el-button>
          <el-button v-if="row.status === 4" text type="danger" @click="handleAudit(row, false)">拒绝退款</el-button>
        </template>
      </el-table-column>
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
        <p><strong>状态：</strong>{{ getStatusText(currentOrder.status) }}</p>
        <p><strong>金额：</strong>¥{{ currentOrder.totalAmount }}</p>
        <p><strong>支付渠道：</strong>{{ currentOrder.payChannel || '-' }}</p>
        <p><strong>创建时间：</strong>{{ currentOrder.createTime }}</p>
        <p><strong>支付时间：</strong>{{ currentOrder.payTime || '-' }}</p>
        <div v-if="currentRefund" style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px;">
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminOrderList, getRefundList, auditRefund } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const detailVisible = ref(false)
const auditVisible = ref(false)
const currentOrder = ref(null)
const currentRefund = ref(null)
const auditApproved = ref(false)
const auditRemark = ref('')
const refundList = ref([])

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

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value !== null) params.status = statusFilter.value
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

onMounted(fetchData)
</script>

<style scoped>
.order-manage { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
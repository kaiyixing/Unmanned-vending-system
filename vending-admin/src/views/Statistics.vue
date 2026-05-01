<template>
  <div class="statistics-page">
    <div class="toolbar">
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        @change="fetchByRange"
      />
    </div>

    <div class="stats-grid">
      <el-card>
        <div class="stat-item">
          <div class="stat-value">{{ overview.totalOrders || 0 }}</div>
          <div class="stat-label">总订单数</div>
        </div>
      </el-card>
      <el-card>
        <div class="stat-item">
          <div class="stat-value">¥{{ (overview.totalSales || 0).toFixed(2) }}</div>
          <div class="stat-label">总销售额</div>
        </div>
      </el-card>
      <el-card>
        <div class="stat-item">
          <div class="stat-value">{{ overview.todayOrders || 0 }}</div>
          <div class="stat-label">今日订单</div>
        </div>
      </el-card>
      <el-card>
        <div class="stat-item">
          <div class="stat-value">¥{{ (overview.todaySales || 0).toFixed(2) }}</div>
          <div class="stat-label">今日销售</div>
        </div>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card>
        <h4 class="mb-3">分类销售占比</h4>
        <div ref="pieRef" style="height: 350px;"></div>
      </el-card>
      <el-card>
        <h4 class="mb-3">货柜销售排行</h4>
        <div ref="barRef" style="height: 350px;"></div>
      </el-card>
    </div>

    <el-card>
      <h4 class="mb-3">热销商品 TOP10</h4>
      <el-table :data="overview.topProducts || []" stripe>
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column label="商品名称">
          <template #default="{ row }">{{ row.productName || row.name }}</template>
        </el-table-column>
        <el-table-column label="销量" width="100">
          <template #default="{ row }">{{ row.totalQuantity || row.quantity || 0 }}</template>
        </el-table-column>
        <el-table-column label="销售额" width="120">
          <template #default="{ row }}">¥{{ (row.totalAmount || row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="mt-4">
      <h4 class="mb-3">退款管理</h4>
      <el-table :data="refunds" stripe>
        <el-table-column prop="refundId" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="reason" label="退款原因" min-width="150" />
        <el-table-column prop="amount" label="退款金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRefundStatusType(row.status)">
              {{ getRefundStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" text type="success" @click="auditRefund(row, true)">通过</el-button>
            <el-button v-if="row.status === 0" text type="danger" @click="auditRefund(row, false)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { statisticsOverview, refundList, refundAudit } from '@/api/statistics'

const dateRange = ref(null)
const overview = ref({})
const refunds = ref([])
const pieRef = ref()
const barRef = ref()

async function fetchData() {
  try {
    const res = await statisticsOverview()
    overview.value = res.data || {}
    renderPieChart(overview.value.salesByCategory || [])
    renderBarChart(overview.value.salesByCabinet || [])
  } catch (e) {}
}

async function fetchByRange() {
  if (!dateRange.value || dateRange.value.length !== 2) return
  try {
    const params = {
      startDate: dateRange.value[0].toISOString().replace('T', ' ').substring(0, 19),
      endDate: dateRange.value[1].toISOString().replace('T', ' ').substring(0, 19)
    }
    const res = await import('@/api/statistics').then(m => m.statisticsByRange(params))
    overview.value = res.data || {}
  } catch (e) {}
}

async function fetchRefunds() {
  try {
    const res = await refundList({ page: 1, size: 50 })
    refunds.value = res.data.records || []
  } catch (e) {}
}

async function auditRefund(row, approved) {
  try {
    const { value } = await ElMessageBox.prompt('审核备注', approved ? '通过退款' : '拒绝退款', {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await refundAudit({ refundId: row.refundId, approved, remark: value || '' })
    ElMessage.success('审核成功')
    fetchRefunds()
  } catch (e) {}
}

function getRefundStatusText(status) {
  return { 0: '申请中', 1: '已通过', 2: '已拒绝', 3: '已退款' }[status] || '未知'
}

function getRefundStatusType(status) {
  return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info'
}

function renderPieChart(categories) {
  if (!pieRef.value || !categories.length) return
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: categories.map(c => ({
        name: c.category || c.name,
        value: c.totalSales || c.totalAmount || 0
      }))
    }],
    color: ['#A2C2E8', '#FFCBA4', '#BEE8D2', '#f5c6c6', '#d4e5f7', '#e0dbd6']
  })
}

function renderBarChart(cabinets) {
  if (!barRef.value || !cabinets.length) return
  const chart = echarts.init(barRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: cabinets.map(c => c.cabinetName || c.name),
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: cabinets.map(c => c.totalSales || c.totalAmount || 0),
      itemStyle: { color: '#A2C2E8', borderRadius: [8, 8, 0, 0] }
    }],
    grid: { bottom: 60 }
  })
}

onMounted(async () => {
  await fetchData()
  fetchRefunds()
  nextTick(() => {
    renderPieChart([])
    renderBarChart([])
  })
})
</script>

<style scoped>
.statistics-page { display: flex; flex-direction: column; gap: 24px; }
.toolbar { display: flex; justify-content: flex-end; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-item { text-align: center; }
.stat-value { font-size: 1.8rem; font-weight: 800; color: var(--admin-primary); }
.stat-label { color: #8a7e74; margin-top: 4px; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.mt-4 { margin-top: 24px; }
</style>
<template>
  <div class="dashboard-page">
    <div class="stats-grid">
      <el-card v-for="stat in stats" :key="stat.label" class="stat-card" @click="handleStatClick(stat)">
        <div class="stat-item">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card class="chart-card">
        <h4 class="mb-3">分类销售占比</h4>
        <div ref="pieRef" style="height: 300px;"></div>
      </el-card>
      <el-card class="chart-card">
        <h4 class="mb-3">货柜销售额排行</h4>
        <div ref="barRef" style="height: 300px;"></div>
      </el-card>
    </div>

    <el-card>
      <h4 class="mb-3">热销商品 TOP10</h4>
      <el-table :data="topProducts" stripe>
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="totalQuantity" label="销量" width="100" />
        <el-table-column prop="totalAmount" label="销售额" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ShoppingBag, Wallet, TrendCharts, Box } from '@element-plus/icons-vue'
import { statisticsOverview } from '@/api/statistics'

const router = useRouter()
const pieRef = ref()
const barRef = ref()
const topProducts = ref([])

const stats = ref([
  { label: '总订单数', value: '-', icon: ShoppingBag, color: '#A2C2E8', type: 'all' },
  { label: '今日订单', value: '-', icon: TrendCharts, color: '#FFCBA4', type: 'today' },
  { label: '总销售额', value: '-', icon: Wallet, color: '#BEE8D2', type: 'all' },
  { label: '今日销售', value: '-', icon: Box, color: '#d4e5f7', type: 'today' }
])

async function fetchData() {
  try {
    const res = await statisticsOverview()
    const data = res.data || {}
    stats.value[0].value = data.totalOrders || 0
    stats.value[1].value = data.todayOrders || 0
    stats.value[2].value = '¥' + (data.totalSales || 0).toFixed(2)
    stats.value[3].value = '¥' + (data.todaySales || 0).toFixed(2)
    topProducts.value = data.topProducts || []
    renderPieChart(data.salesByCategory || [])
    renderBarChart(data.salesByCabinet || [])
  } catch (e) {
    console.error(e)
  }
}

function renderPieChart(categories) {
  if (!pieRef.value || categories.length === 0) return
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: categories.map((c, i) => ({
        name: c.category || c.name,
        value: c.totalSales || c.totalAmount || 0
      })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } }
    }],
    color: ['#A2C2E8', '#FFCBA4', '#BEE8D2', '#f5c6c6', '#d4e5f7', '#e0dbd6']
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderBarChart(cabinets) {
  if (!barRef.value || cabinets.length === 0) return
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
  window.addEventListener('resize', () => chart.resize())
}

function handleStatClick(stat) {
  // 跳转到订单管理页面
  router.push({
    path: '/order',
    query: {
      type: stat.type
    }
  })
}

onMounted(async () => {
  await fetchData()
  nextTick(() => {
    renderPieChart([])
    renderBarChart([])
  })
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--admin-white);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 800;
}

.stat-label {
  color: #8a7e74;
  font-size: 0.9rem;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card { min-height: 360px; }

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-row { grid-template-columns: 1fr; }
}
</style>
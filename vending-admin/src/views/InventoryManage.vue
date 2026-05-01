<template>
  <div class="inventory-manage">
    <div class="alerts-bar" v-if="alerts.length > 0">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <span>⚠️ 以下 {{ alerts.length }} 条库存低于预警阈值，请及时补货</span>
        </template>
      </el-alert>
    </div>

    <div class="toolbar">
      <el-select v-model="cabinetFilter" placeholder="选择货柜" clearable style="width: 200px" @change="fetchData">
        <el-option v-for="c in cabinets" :key="c.cabinetId" :label="c.name" :value="c.cabinetId" />
      </el-select>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="inventoryId" label="ID" width="80" />
      <el-table-column prop="cabinetId" label="货柜ID" width="100" />
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column label="库存数量" width="150">
        <template #default="{ row }">
          <el-input-number
            v-model="row.quantity"
            :min="0"
            size="small"
            @change="handleUpdate(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="预警阈值" width="150">
        <template #default="{ row }">
          <el-input-number
            v-model="row.threshold"
            :min="0"
            size="small"
            @change="handleUpdate(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.quantity <= row.threshold ? 'danger' : 'success'">
            {{ row.quantity <= row.threshold ? '库存不足' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      @current-change="fetchData"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminInventoryList, adminInventoryAlerts, adminInventoryUpdate } from '@/api/inventory'
import { adminCabinetList } from '@/api/cabinet'

const loading = ref(false)
const tableData = ref([])
const alerts = ref([])
const cabinets = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const cabinetFilter = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (cabinetFilter.value) params.cabinetId = cabinetFilter.value
    const res = await adminInventoryList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function fetchAlerts() {
  try {
    const res = await adminInventoryAlerts()
    alerts.value = res.data || []
  } catch (e) {}
}

async function fetchCabinets() {
  try {
    const res = await adminCabinetList({ page: 1, size: 100 })
    cabinets.value = res.data.records || []
  } catch (e) {}
}

async function handleUpdate(row) {
  try {
    await adminInventoryUpdate({
      inventoryId: row.inventoryId,
      quantity: row.quantity,
      threshold: row.threshold
    })
    ElMessage.success('更新成功')
  } catch (e) {
  }
}

onMounted(() => {
  fetchData()
  fetchAlerts()
  fetchCabinets()
})
</script>

<style scoped>
.inventory-manage { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.alerts-bar :deep(.el-alert__title) { font-size: 0.9rem; }
</style>
<template>
  <div class="inventory-manage">
    <div class="alerts-bar" v-if="alerts.length > 0">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <span>⚠️ 以下 {{ alerts.length }} 条库存低于预警阈值，请及时补货</span>
        </template>
      </el-alert>
    </div>

    <div class="cabinet-tabs">
      <div class="section-title">
        <span>选择货柜：</span>
      </div>
      <div class="cabinet-selector">
        <el-select v-model="cabinetFilter" placeholder="请选择货柜" clearable style="width: 280px" @change="fetchData">
          <el-option v-for="c in cabinets" :key="c.cabinetId" :label="c.name + ' - ' + c.city" :value="c.cabinetId" />
        </el-select>
        <el-button type="primary" :disabled="!cabinetFilter" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加商品到货柜
        </el-button>
      </div>
    </div>

    <div v-if="!cabinetFilter" class="cabinet-grid">
      <el-card v-for="cabinet in cabinetsWithInventory" :key="cabinet.cabinetId" class="cabinet-card">
        <template #header>
          <div class="cabinet-header">
            <div class="cabinet-info">
              <span class="cabinet-name">{{ cabinet.name }}</span>
              <el-tag size="small" type="success">{{ cabinet.city }}</el-tag>
            </div>
            <span class="product-count">{{ cabinet.items.length }} 个商品</span>
          </div>
        </template>
        <el-table :data="cabinet.items" stripe size="small">
          <el-table-column prop="productName" label="商品名称" min-width="120" />
          <el-table-column prop="category" label="分类" width="80" />
          <el-table-column label="库存" width="130">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="0"
                size="small"
                @change="handleUpdate(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="预警" width="100">
            <template #default="{ row }">
              <el-input-number
                v-model="row.threshold"
                :min="0"
                size="small"
                @change="handleUpdate(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.quantity <= row.threshold ? 'danger' : 'success'" size="small">
                {{ row.quantity <= row.threshold ? '不足' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div v-else class="current-cabinet">
      <el-card class="cabinet-card single">
        <template #header>
          <div class="cabinet-header">
            <div class="cabinet-info">
              <span class="cabinet-name">{{ currentCabinetName }}</span>
              <el-tag size="small" type="success">{{ currentCabinetCity }}</el-tag>
            </div>
            <span class="product-count">{{ tableData.length }} 个商品</span>
          </div>
        </template>
        <el-table :data="tableData" stripe>
          <el-table-column prop="productName" label="商品名称" min-width="150" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="price" label="售价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="库存" width="150">
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
      </el-card>
    </div>

    <el-dialog v-model="addDialogVisible" title="添加商品到货柜" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="选择商品">
          <el-select v-model="addForm.productId" placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="p in availableProducts"
              :key="p.productId"
              :label="p.name + ' (' + p.category + ')'"
              :value="p.productId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="初始库存">
          <el-input-number v-model="addForm.quantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="addForm.threshold" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="addLoading">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminInventoryList, adminInventoryAlerts, adminInventoryUpdate, adminInventoryAdd } from '@/api/inventory'
import { adminCabinetList } from '@/api/cabinet'
import { adminProductList } from '@/api/product'

const loading = ref(false)
const addLoading = ref(false)
const tableData = ref([])
const alerts = ref([])
const cabinets = ref([])
const allProducts = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const cabinetFilter = ref(null)
const addDialogVisible = ref(false)
const allInventoryData = ref([])

const addForm = reactive({
  productId: null,
  quantity: 0,
  threshold: 5
})

const currentCabinetName = computed(() => {
  const cabinet = cabinets.value.find(c => c.cabinetId === cabinetFilter.value)
  return cabinet ? cabinet.name : ''
})

const currentCabinetCity = computed(() => {
  const cabinet = cabinets.value.find(c => c.cabinetId === cabinetFilter.value)
  return cabinet ? cabinet.city : ''
})

const availableProducts = computed(() => {
  if (!cabinetFilter.value) return allProducts.value
  const currentProductIds = tableData.value.map(inv => inv.productId)
  return allProducts.value.filter(p => !currentProductIds.includes(p.productId))
})

const cabinetsWithInventory = computed(() => {
  return cabinets.value.map(cabinet => {
    const items = allInventoryData.value
      .filter(inv => inv.cabinetId === cabinet.cabinetId)
      .map(inv => {
        const product = allProducts.value.find(p => p.productId === inv.productId)
        return {
          ...inv,
          productName: product ? product.name : `商品#${inv.productId}`,
          category: product ? product.category : '-',
          price: product ? product.price : 0
        }
      })
    return {
      ...cabinet,
      items
    }
  }).filter(c => c.items.length > 0)
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (cabinetFilter.value) params.cabinetId = cabinetFilter.value
    const res = await adminInventoryList(params)
    const records = res.data.records || []
    tableData.value = records.map(inv => {
      const product = allProducts.value.find(p => p.productId === inv.productId)
      return {
        ...inv,
        productName: product ? product.name : `商品#${inv.productId}`,
        category: product ? product.category : '-',
        price: product ? product.price : 0
      }
    })
    total.value = res.data.total || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function fetchAllInventory() {
  try {
    const res = await adminInventoryList({ page: 1, size: 1000 })
    allInventoryData.value = res.data.records || []
  } catch (e) {}
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

async function fetchAllProducts() {
  try {
    const res = await adminProductList({ page: 1, size: 100 })
    allProducts.value = res.data.records || []
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

function openAddDialog() {
  addForm.productId = null
  addForm.quantity = 0
  addForm.threshold = 5
  addDialogVisible.value = true
}

async function handleAdd() {
  if (!addForm.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  addLoading.value = true
  try {
    await adminInventoryAdd({
      cabinetId: cabinetFilter.value,
      productId: addForm.productId,
      quantity: addForm.quantity,
      threshold: addForm.threshold
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    fetchData()
    fetchAllInventory()
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  } finally {
    addLoading.value = false
  }
}

onMounted(() => {
  fetchAllProducts().then(() => {
    fetchCabinets()
    fetchAllInventory()
    fetchData()
  })
  fetchAlerts()
})
</script>

<style scoped>
.inventory-manage {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.alerts-bar :deep(.el-alert__title) {
  font-size: 0.9rem;
}

.cabinet-tabs {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: var(--admin-white);
  border-radius: 16px;
  box-shadow: var(--shadow-clay);
}

.section-title {
  font-weight: 600;
  color: var(--admin-text);
  white-space: nowrap;
}

.cabinet-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cabinet-selector :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

.cabinet-selector :deep(.el-button) {
  border-radius: 12px;
}

.cabinet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
}

.cabinet-card {
  border-radius: 16px;
  box-shadow: var(--shadow-clay);
  border: none;
}

.cabinet-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: linear-gradient(135deg, #d6e8fc 0%, #b6d4fb 100%);
  border-bottom: none;
}

.cabinet-card.single :deep(.el-card__header) {
  background: linear-gradient(135deg, #d6e8fc 0%, #b6d4fb 100%);
}

.cabinet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cabinet-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cabinet-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--admin-text);
}

.cabinet-info .el-tag {
  background: rgba(255, 255, 255, 0.9);
  border: none;
}

.product-count {
  font-size: 0.85rem;
  color: var(--admin-text);
  opacity: 0.8;
}

.current-cabinet {
  max-width: 1000px;
}

.current-cabinet .cabinet-card {
  width: 100%;
}

.cabinet-card :deep(.el-card__body) {
  padding: 16px;
}

.cabinet-card :deep(.el-table .el-input-number) {
  width: 110px;
}

.cabinet-card :deep(.el-table .el-input-number .el-input__wrapper) {
  border-radius: 8px;
}
</style>

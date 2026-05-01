<template>
  <div class="product-manage">
    <div class="toolbar">
      <el-input v-model="searchCategory" placeholder="搜索分类" clearable style="width: 200px" @keyup.enter="fetchData" />
      <div class="toolbar-right">
        <el-button type="primary" @click="openDialog()">新增商品</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="productId" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="spec" label="规格" width="120" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      @current-change="fetchData"
    />

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成本价">
          <el-input-number v-model="form.costPrice" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminProductList, adminProductSave, adminProductUpdate, adminProductDelete } from '@/api/product'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const searchCategory = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  productId: null,
  name: '',
  category: '',
  price: 0,
  costPrice: 0,
  spec: '',
  description: '',
  status: 1
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchCategory.value) params.category = searchCategory.value
    const res = await adminProductList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.keys(form).forEach(k => form[k] = row[k] ?? '')
  } else {
    Object.keys(form).forEach(k => form[k] = k === 'status' ? 1 : '')
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const api = isEdit.value ? adminProductUpdate : adminProductSave
    await api({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此商品吗？')
    await adminProductDelete(row.productId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

onMounted(fetchData)
</script>

<style scoped>
.product-manage { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
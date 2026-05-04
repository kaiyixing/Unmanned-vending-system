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
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            style="width: 50px; height: 50px; border-radius: 8px;"
            fit="cover"
            :preview-src-list="[row.imageUrl]"
            preview-teleported
          />
          <span v-else style="color: #ccc; font-size: 12px;">暂无</span>
        </template>
      </el-table-column>
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
      <template #empty>
        <el-empty description="暂无商品数据" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      @current-change="fetchData"
    />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="cabinet-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUploadProduct"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" style="width: 100%; height: 200px; object-fit: cover; border-radius: 12px;" />
            <el-icon v-else class="cabinet-uploader-icon" :size="40"><Plus /></el-icon>
          </el-upload>
          <div v-if="form.imageUrl" class="image-preview">
            <el-button type="danger" size="small" link @click="removeImage">删除图片</el-button>
          </div>
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
import { Plus } from '@element-plus/icons-vue'
import { adminProductList, adminProductSave, adminProductUpdate, adminProductDelete } from '@/api/product'
import { uploadImageWithPrefix } from '@/api/pickup'

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
  imageUrl: '',
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

async function handleUploadProduct(options) {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('prefix', 'products')
    const res = await uploadImageWithPrefix(formData)
    form.imageUrl = res.data.url || res.data || res
    ElMessage.success('图片上传成功')
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB!')
    return false
  }
  return true
}

function removeImage() {
  form.imageUrl = ''
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
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--admin-white);
  border-radius: 16px;
  box-shadow: var(--shadow-clay);
}

.cabinet-uploader :deep(.el-upload) {
  border: 1px dashed var(--admin-border);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100%;
}

.cabinet-uploader :deep(.el-upload:hover) {
  border-color: var(--admin-primary);
}

.cabinet-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 200px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview {
  margin-top: 8px;
}
</style>

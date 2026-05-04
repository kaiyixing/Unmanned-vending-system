<template>
  <div class="cabinet-manage">
    <div class="toolbar">
      <el-input v-model="searchCity" placeholder="搜索城市" clearable style="width: 200px" @keyup.enter="fetchData" />
      <div class="toolbar-right">
        <el-button type="primary" @click="openDialog()">新增货柜</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="cabinetId" label="ID" width="80" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width: 60px; height: 60px; border-radius: 4px" fit="cover" />
          <span v-else style="color: #ccc">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="cabinetCode" label="编号" width="120" />
      <el-table-column prop="name" label="货柜名称" min-width="150" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="address" label="地址" min-width="200" />
      <el-table-column prop="capacity" label="容量" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'">
            {{ { 0: '停用', 1: '营业中', 2: '维护中' }[row.status] }}
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
        <el-empty description="暂无货柜数据" />
      </template>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      @current-change="fetchData"
    />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑货柜' : '新增货柜'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编号">
          <el-input v-model="form.cabinetCode" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="cabinet-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width: 100%; height: 200px" fit="cover" />
            <el-icon v-else class="cabinet-uploader-icon" :size="40"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="0" label="停用" />
            <el-option :value="1" label="营业中" />
            <el-option :value="2" label="维护中" />
          </el-select>
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
import { adminCabinetList, adminCabinetSave, adminCabinetUpdate, adminCabinetDelete } from '@/api/cabinet'
import { uploadImage, uploadImageWithPrefix } from '@/api/pickup'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const searchCity = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  cabinetId: null,
  cabinetCode: '',
  name: '',
  city: '',
  address: '',
  imageUrl: '',
  capacity: 50,
  status: 1
})

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！')
    return false
  }
  return true
}

async function handleUpload(options) {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('prefix', 'cabinets')
    const res = await uploadImageWithPrefix(formData)
    form.imageUrl = res.data.url || res.data || res
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchCity.value) params.city = searchCity.value
    const res = await adminCabinetList(params)
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
    Object.keys(form).forEach(k => form[k] = k === 'capacity' ? 50 : k === 'status' ? 1 : '')
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const api = isEdit.value ? adminCabinetUpdate : adminCabinetSave
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
    await ElMessageBox.confirm('确定删除此货柜吗？')
    await adminCabinetDelete(row.cabinetId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

onMounted(fetchData)
</script>

<style scoped>
.cabinet-manage { display: flex; flex-direction: column; gap: 16px; }
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
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
<template>
  <div class="cabinet-card clay-box" @click="$emit('click')">
    <div class="cabinet-map">
      <el-icon :size="48" color="var(--color-primary)"><Location /></el-icon>
    </div>
    <div class="cabinet-info">
      <h4>{{ cabinet.name }}</h4>
      <p class="address"><el-icon><Location /></el-icon> {{ cabinet.city }}&nbsp;&nbsp;{{ cabinet.address }}</p>
      <div class="status-row">
        <span :class="['badge-clay', statusClass]">{{ statusText }}</span>
        <small class="text-muted">{{ cabinet.cabinetCode }}</small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Location } from '@element-plus/icons-vue'

const props = defineProps({
  cabinet: { type: Object, required: true }
})

defineEmits(['click'])

const statusClass = computed(() => {
  return {
    0: 'badge-danger',
    1: 'badge-success',
    2: 'badge-warning'
  }[props.cabinet.status] || 'badge-info'
})

const statusText = computed(() => {
  return {
    0: '停用',
    1: '营业中',
    2: '维护中'
  }[props.cabinet.status] || '未知'
})
</script>

<style scoped>
.cabinet-card {
  overflow: hidden;
  cursor: pointer;
}

.cabinet-map {
  height: 160px;
  background: linear-gradient(135deg, #e9f1fa 0%, #d4e5f7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cabinet-info {
  padding: 16px;
}

.cabinet-info h4 {
  font-size: 1.1rem;
  margin-bottom: 6px;
}

.address {
  font-size: 0.9rem;
  color: #8a7e74;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
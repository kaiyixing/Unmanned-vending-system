<template>
  <div class="product-card clay-box">
    <div class="product-img">
      <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name">
      <div v-else class="img-placeholder">
        <i class="bi bi-cup-hot" style="font-size: 3rem;"></i>
      </div>
      <span v-if="product.status === 0" class="off-shelf-tag">已下架</span>
    </div>
    <div class="product-info">
      <h5>{{ product.name }}</h5>
      <p class="spec">{{ product.spec || '' }}</p>
      <div class="price-row">
        <span class="price">¥{{ product.price }}</span>
        <button
          v-if="product.status === 1 && product.stock > 0"
          class="btn-clay btn-sm btn-primary add-btn"
          @click="$emit('add')"
        >
          + 加购
        </button>
        <span v-else-if="product.stock === 0" class="no-stock">已售罄</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: { type: Object, required: true }
})

defineEmits(['add'])
</script>

<style scoped>
.product-card {
  overflow: hidden;
  cursor: pointer;
}

.product-img {
  position: relative;
  height: 180px;
  background: linear-gradient(135deg, #f8f6f4 0%, #ede8e3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.off-shelf-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--color-dark-soft);
  color: var(--color-text);
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
}

.product-info {
  padding: 16px;
}

.product-info h5 {
  font-size: 1rem;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spec {
  font-size: 0.85rem;
  color: #8a7e74;
  margin-bottom: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 1.2rem;
  font-weight: 800;
  color: var(--color-primary);
}

.no-stock {
  font-size: 0.85rem;
  color: #c0392b;
}

.add-btn {
  padding: 6px 14px;
  font-size: 0.85rem;
}
</style>
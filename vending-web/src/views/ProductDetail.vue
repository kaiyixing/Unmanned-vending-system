<template>
  <div class="detail-page">
    <NavBar />
    <div class="page-content">
      <div class="container" v-loading="loading">
        <button class="btn-clay btn-sm mb-4" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </button>
        <div class="detail-layout" v-if="product">
          <div class="detail-img clay-box">
            <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name">
            <div v-else class="img-placeholder"><el-icon :size="80"><ShoppingBag /></el-icon></div>
          </div>
          <div class="detail-info clay-box">
            <h2>{{ product.name }}</h2>
            <p class="text-muted">{{ product.description || '暂无描述' }}</p>
            <div class="spec-tag">{{ product.spec || '' }}</div>
            <div class="price">¥{{ product.price }}</div>
            <div class="stock-info">库存：{{ product.stock }}件</div>
            <div class="action-row">
              <div class="quantity-ctrl">
                <button class="btn-clay btn-sm" @click="qty = Math.max(1, qty - 1)">-</button>
                <span class="qty-num">{{ qty }}</span>
                <button class="btn-clay btn-sm" @click="qty = Math.min(product.stock, qty + 1)">+</button>
              </div>
              <button v-if="cabinetId" class="btn-clay btn-primary btn-lg" @click="handleAddToCart" :disabled="product.stock === 0">
                加入购物车
              </button>
              <button v-else class="btn-clay btn-primary btn-lg" @click="$router.back()">
                返回货柜选购
              </button>
            </div>
            <p v-if="!cabinetId" class="text-muted mt-2" style="font-size: 0.9rem;">
              * 请从货柜商品列表页面添加商品到购物车
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingBag } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useCartStore } from '@/stores/cart'
import { getProductById } from '@/api/product'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const product = ref(null)
const loading = ref(false)
const qty = ref(1)
const cabinetId = ref(route.query.cabinetId ? Number(route.query.cabinetId) : null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getProductById(route.params.id)
    product.value = res.data
    product.value.stock = product.value.inventory?.quantity || 0
  } catch (e) {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

function handleAddToCart() {
  if (!cabinetId.value) {
    ElMessage.warning('请返回货柜页面添加商品')
    return
  }
  let addedCount = 0
  let hasConflict = false
  let hasOutOfStock = false
  
  for (let i = 0; i < qty.value; i++) {
    const result = cartStore.addToCart(product.value, cabinetId.value, route.query.cabinetName || '')
    if (result.success) {
      addedCount++
    } else if (result.reason === 'cabinet_conflict') {
      hasConflict = true
      break
    } else if (result.reason === 'out_of_stock') {
      hasOutOfStock = true
      break
    }
  }
  
  if (hasConflict) {
    ElMessage.warning('请先清空当前货柜的购物车')
  } else if (hasOutOfStock) {
    ElMessage.warning('库存不足')
  } else if (addedCount > 0) {
    ElMessage.success(`已添加 ${addedCount} 件到购物车`)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-content { padding-top: 80px; min-height: 100vh; }

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.detail-img {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.detail-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.img-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
}

.detail-info {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.spec-tag {
  display: inline-block;
  background: #e9f1fa;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  width: fit-content;
}

.price {
  font-size: 2rem;
  font-weight: 800;
  color: var(--color-primary);
}

.stock-info { color: #8a7e74; }

.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
}

.quantity-ctrl {
  display: flex;
  align-items: center;
  gap: 12px;
}

.qty-num {
  font-size: 1.2rem;
  font-weight: 700;
  min-width: 30px;
  text-align: center;
}

@media (max-width: 768px) {
  .detail-layout { grid-template-columns: 1fr; }
  .detail-img { height: 250px; }
}
</style>
<template>
  <div class="cabinet-page">
    <NavBar />
    <div class="page-content">
      <div class="container" v-loading="loading">
        <div class="cabinet-header mb-4">
          <button class="btn-clay btn-sm" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </button>
          <h2>{{ cabinetName }}</h2>
          <p class="text-muted">{{ cabinetAddress }}</p>
        </div>

        <!-- Category Filter -->
        <div class="category-bar mb-4">
          <button
            v-for="cat in categories"
            :key="cat"
            class="btn-clay btn-sm"
            :class="{ 'btn-primary': activeCategory === cat }"
            @click="activeCategory = cat"
          >
            {{ cat }}
          </button>
        </div>

        <!-- Product Grid -->
        <div class="grid-4" v-if="filteredProducts.length > 0">
          <ProductCard
            v-for="product in filteredProducts"
            :key="product.productId"
            :product="product"
            @add="handleAddToCart(product)"
          />
        </div>
        <el-empty v-else description="暂无商品" />

        <!-- Cart Float Button -->
        <div class="float-cart" v-if="cartStore.totalCount > 0" @click="$router.push('/cart')">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span class="cart-count">{{ cartStore.totalCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import ProductCard from '@/components/ProductCard.vue'
import { useCartStore } from '@/stores/cart'
import { getCabinetProducts, getCabinetById } from '@/api/cabinet'

const route = useRoute()
const cartStore = useCartStore()
const cabinetId = route.params.id

const loading = ref(false)
const products = ref([])
const cabinetName = ref('')
const cabinetAddress = ref('')
const activeCategory = ref('全部')
const categories = ref(['全部'])

const filteredProducts = computed(() => {
  if (activeCategory.value === '全部') return products.value
  return products.value.filter(p => p.category === activeCategory.value)
})

async function fetchData() {
  loading.value = true
  try {
    const [cabRes, prodRes] = await Promise.all([
      getCabinetById(cabinetId),
      getCabinetProducts(cabinetId)
    ])
    cabinetName.value = cabRes.data?.name || ''
    cabinetAddress.value = `${cabRes.data?.city || ''}${cabRes.data?.address || ''}`
    products.value = (prodRes.data || []).map(p => ({
      ...p,
      stock: p.inventory?.quantity || 0
    }))
    const cats = [...new Set(products.value.map(p => p.category))]
    categories.value = ['全部', ...cats]
  } catch (e) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

function handleAddToCart(product) {
  const success = cartStore.addToCart(product, Number(cabinetId), cabinetName.value)
  if (!success) {
    ElMessage.warning(cartStore.cabinetId && cartStore.cabinetId !== Number(cabinetId)
      ? '请先清空当前货柜的购物车'
      : '库存不足')
  } else {
    ElMessage.success('已加入购物车')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-content {
  padding-top: 80px;
  min-height: 100vh;
}

.cabinet-header {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.category-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.float-cart {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background-color: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-white);
  box-shadow: var(--shadow-clay-deep);
  cursor: pointer;
  transition: var(--transition-squish);
  z-index: 100;
}

.float-cart:hover {
  transform: scale(1.1);
}

.cart-count {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: var(--color-secondary);
  color: var(--color-text);
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 800;
}
</style>
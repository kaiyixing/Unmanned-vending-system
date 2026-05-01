<template>
  <div class="cart-page">
    <NavBar />
    <div class="page-content">
      <div class="container">
        <div class="cart-header mb-4">
          <h2>购物车</h2>
          <span class="text-muted" v-if="cartStore.cabinetName">货柜：{{ cartStore.cabinetName }}</span>
        </div>

        <div v-if="cartStore.items.length > 0">
          <div class="cart-items">
            <div class="cart-item clay-box" v-for="item in cartStore.items" :key="item.productId">
              <div class="item-img">
                <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.name">
                <div v-else class="img-placeholder"><el-icon :size="40"><ShoppingBag /></el-icon></div>
              </div>
              <div class="item-info">
                <h4>{{ item.name }}</h4>
                <p class="text-muted">{{ item.spec }}</p>
                <div class="item-price">¥{{ item.price }}</div>
              </div>
              <div class="item-actions">
                <div class="quantity-ctrl">
                  <button class="btn-clay btn-sm" @click="cartStore.updateQuantity(item.productId, item.quantity - 1)">-</button>
                  <span class="qty-num">{{ item.quantity }}</span>
                  <button class="btn-clay btn-sm" @click="cartStore.updateQuantity(item.productId, item.quantity + 1)">+</button>
                </div>
                <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                <button class="btn-clay btn-sm" style="color: #c0392b;" @click="cartStore.removeFromCart(item.productId)">
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
            </div>
          </div>

          <div class="cart-footer clay-box">
            <div class="summary">
              <span class="text-muted">共 {{ cartStore.totalCount }} 件商品</span>
              <span class="total-price">合计：¥{{ cartStore.totalPrice.toFixed(2) }}</span>
            </div>
            <button class="btn-clay btn-primary btn-lg" @click="$router.push('/checkout')">去结算</button>
          </div>
        </div>

        <el-empty v-else description="购物车空空如也">
          <button class="btn-clay btn-primary" @click="$router.push('/')">去逛逛</button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ShoppingBag, Delete } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { useCartStore } from '@/stores/cart'

const cartStore = useCartStore()
</script>

<style scoped>
.page-content { padding-top: 80px; min-height: 100vh; }

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  background: #f0ede9;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 1rem;
  margin-bottom: 4px;
}

.item-price {
  font-weight: 800;
  color: var(--color-primary);
  font-size: 1.1rem;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quantity-ctrl {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-num {
  font-weight: 700;
  min-width: 24px;
  text-align: center;
}

.item-subtotal {
  font-weight: 800;
  font-size: 1.1rem;
  min-width: 80px;
  text-align: right;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
}

.summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.total-price {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .cart-item { flex-wrap: wrap; }
  .item-actions { width: 100%; justify-content: space-between; }
}
</style>
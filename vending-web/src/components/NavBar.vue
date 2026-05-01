<template>
  <header class="nav-header" :class="{ 'scrolled': scrolled }">
    <div class="nav-inner">
      <a href="/" class="nav-brand" @click.prevent="goHome">智能货柜</a>
      <nav class="nav-links">
        <a href="/" @click.prevent="goHome">首页</a>
        <a href="/cart" @click.prevent="$router.push('/cart')">
          <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" class="cart-badge">
            <span>购物车</span>
          </el-badge>
        </a>
        <a href="/orders" @click.prevent="$router.push('/orders')">我的订单</a>
        <a href="/profile" @click.prevent="$router.push('/profile')">个人中心</a>
      </nav>
      <div class="nav-right">
        <template v-if="userStore.token">
          <span class="user-name">{{ userStore.userInfo.username || '用户' }}</span>
          <button class="btn-clay btn-sm" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <button class="btn-clay btn-sm btn-primary" @click="$router.push('/login')">登录</button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const scrolled = ref(false)

function handleScroll() {
  scrolled.value = window.scrollY > 20
}

function goHome() {
  router.push('/')
}

function handleLogout() {
  userStore.logout()
  cartStore.clearCart()
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.nav-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  padding: 16px 0;
  transition: all 0.3s ease;
}

.nav-header.scrolled {
  background-color: rgba(244, 241, 238, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  padding: 10px 0;
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-text);
}

.nav-brand:hover {
  color: var(--color-primary);
}

.nav-links {
  display: flex;
  gap: 24px;
  align-items: center;
}

.nav-links a {
  font-weight: 600;
  color: var(--color-text);
  transition: var(--transition-squish);
}

.nav-links a:hover {
  color: var(--color-primary);
}

.cart-badge :deep(.el-badge__content) {
  background-color: var(--color-secondary);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 600;
  color: var(--color-text);
}

@media (max-width: 768px) {
  .nav-links { display: none; }
  .nav-right { gap: 8px; }
}
</style>
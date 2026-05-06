import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/cabinet/:id', name: 'CabinetProducts', component: () => import('@/views/CabinetProducts.vue') },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
  { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue') },
  { path: '/checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue') },
  { path: '/pickup-code/:orderId', name: 'PickupCode', component: () => import('@/views/PickupCode.vue') },
  { path: '/orders', name: 'Orders', component: () => import('@/views/Orders.vue') },
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function hasValidToken() {
  const token = localStorage.getItem('accessToken') || localStorage.getItem('token')
  return token && token.length > 0
}

router.beforeEach((to, from, next) => {
  const hasToken = hasValidToken()
  if (to.path !== '/login' && !hasToken) {
    next('/login')
  } else if (to.path === '/login' && hasToken) {
    next('/')
  } else {
    next()
  }
})

export default router

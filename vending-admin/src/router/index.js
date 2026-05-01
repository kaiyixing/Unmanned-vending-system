import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'AdminLogin', component: () => import('@/views/AdminLogin.vue') },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'product', name: 'ProductManage', component: () => import('@/views/ProductManage.vue') },
      { path: 'cabinet', name: 'CabinetManage', component: () => import('@/views/CabinetManage.vue') },
      { path: 'inventory', name: 'InventoryManage', component: () => import('@/views/InventoryManage.vue') },
      { path: 'order', name: 'OrderManage', component: () => import('@/views/OrderManage.vue') },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/Statistics.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('adminToken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
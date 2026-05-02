<template>
  <div class="admin-layout">
    <el-aside width="220px" class="admin-sidebar">
      <div class="sidebar-header">
        <h3>智能货柜</h3>
      </div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/product">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/cabinet">
          <el-icon><Shop /></el-icon>
          <span>货柜管理</span>
        </el-menu-item>
        <el-menu-item index="/inventory">
          <el-icon><Box /></el-icon>
          <span>库存管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/pickup-verify">
          <el-icon><Ticket /></el-icon>
          <span>取货验证</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <div class="admin-main">
      <header class="admin-header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <span>{{ userStore.userInfo.username }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { DataAnalysis, Goods, Shop, Box, List, TrendCharts, Ticket } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const titleMap = {
  '/dashboard': '数据看板',
  '/product': '商品管理',
  '/cabinet': '货柜管理',
  '/inventory': '库存管理',
  '/order': '订单管理',
  '/pickup-verify': '取货验证',
  '/statistics': '数据统计'
}

const pageTitle = computed(() => titleMap[route.path] || '管理后台')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}

.admin-sidebar {
  background: var(--admin-bg);
  border-right: 1px solid var(--admin-border);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid var(--admin-border);
}

.sidebar-header h3 {
  font-size: 1.3rem;
  font-weight: 800;
  margin: 0;
}

.sidebar-menu {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: var(--admin-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--admin-white);
  border-bottom: 1px solid var(--admin-border);
}

.page-title {
  font-size: 1.1rem;
  font-weight: 700;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--admin-bg);
}
</style>
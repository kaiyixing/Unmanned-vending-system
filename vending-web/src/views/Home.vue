<template>
  <div class="home-page">
    <NavBar />

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-blobs">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
      </div>
      <div class="container">
        <div class="hero-content text-center">
          <h1>线上选购 <span class="text-secondary">自助取货</span> 便捷购物</h1>
          <p class="lead mt-4 mb-5">24小时无人值守，扫码即取，智能便捷的新零售体验</p>
          <button class="btn-clay btn-primary btn-lg" @click="scrollToCabinets">查看附近货柜</button>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section">
      <div class="container">
        <div class="grid-4 text-center">
          <div class="clay-box p-4">
            <div class="stat-value">{{ cabinets.length }}</div>
            <p class="mb-0 fw-bold">合作货柜</p>
          </div>
          <div class="clay-box p-4">
            <div class="stat-value">50+</div>
            <p class="mb-0 fw-bold">商品种类</p>
          </div>
          <div class="clay-box p-4">
            <div class="stat-value">10000+</div>
            <p class="mb-0 fw-bold">注册用户</p>
          </div>
          <div class="clay-box p-4">
            <div class="stat-value">99%</div>
            <p class="mb-0 fw-bold">满意度</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Cabinets Section -->
    <section class="section-padding" id="cabinets" ref="cabinetsRef">
      <div class="container">
        <div class="text-center mb-5">
          <h2>附近货柜</h2>
          <p class="lead text-muted">选择距离您最近的智能货柜，开启便捷购物体验</p>
        </div>
        <div v-loading="loading">
          <div class="grid-3" v-if="cabinets.length > 0">
            <CabinetCard
              v-for="cabinet in cabinets"
              :key="cabinet.cabinetId"
              :cabinet="cabinet"
              @click="goCabinet(cabinet.cabinetId)"
            />
          </div>
          <el-empty v-else description="暂无货柜数据" />
        </div>
      </div>
    </section>

    <!-- How It Works -->
    <section class="section-padding" style="background-color: #e9f1fa;">
      <div class="container">
        <div class="text-center mb-5">
          <h2>购物流程</h2>
          <p class="lead text-muted">四步轻松完成购物</p>
        </div>
        <div class="grid-4">
          <div class="clay-box step-card">
            <div class="step-num">1</div>
            <div class="step-icon">📱</div>
            <h4>登录注册</h4>
            <p class="text-muted mb-0">使用账号登录或注册新账号</p>
          </div>
          <div class="clay-box step-card">
            <div class="step-num">2</div>
            <div class="step-icon">🏪</div>
            <h4>选择货柜</h4>
            <p class="text-muted mb-0">浏览附近货柜，选择商品加入购物车</p>
          </div>
          <div class="clay-box step-card">
            <div class="step-num">3</div>
            <div class="step-icon">💰</div>
            <h4>在线支付</h4>
            <p class="text-muted mb-0">使用微信或支付宝完成支付</p>
          </div>
          <div class="clay-box step-card">
            <div class="step-num">4</div>
            <div class="step-icon">📦</div>
            <h4>取货码取货</h4>
            <p class="text-muted mb-0">获取取货码，到指定货柜取货</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="container">
        <div class="grid-2">
          <div>
            <h4 class="mb-3">联系我们</h4>
            <p class="text-muted">客服电话：400-888-9999</p>
            <p class="text-muted">电子邮箱：support@vending.com</p>
          </div>
          <div class="text-right" style="text-align: right;">
            <p class="mb-0 text-muted">© 2026 智能货柜 版权所有</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import CabinetCard from '@/components/CabinetCard.vue'
import { getCabinetList } from '@/api/cabinet'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cabinets = ref([])
const loading = ref(false)
const cabinetsRef = ref()

async function fetchCabinets() {
  loading.value = true
  try {
    cabinets.value = await getCabinetList()
  } catch (e) {
    ElMessage.error('获取货柜列表失败')
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push('/login')
}

function scrollToCabinets() {
  cabinetsRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function goCabinet(id) {
  router.push(`/cabinet/${id}`)
}

onMounted(fetchCabinets)
</script>

<style scoped>
.hero-section {
  padding-top: 140px;
  min-height: 70vh;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.hero-blobs {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-blobs .blob {
  position: absolute;
  opacity: 0.7;
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
  animation: float 20s ease-in-out infinite;
}

.hero-blobs .blob-1 {
  width: 250px;
  height: 250px;
  background-color: var(--color-primary);
  top: 15%;
  left: 5%;
}

.hero-blobs .blob-2 {
  width: 350px;
  height: 350px;
  background-color: var(--color-secondary);
  bottom: 5%;
  right: 8%;
  animation-delay: -7s;
}

.hero-blobs .blob-3 {
  width: 200px;
  height: 200px;
  background-color: var(--color-accent);
  top: 30%;
  right: 25%;
  animation-delay: -12s;
}

@keyframes float {
  0% { transform: translateY(0px) rotate(-2deg); }
  50% { transform: translateY(-35px) rotate(2deg); }
  100% { transform: translateY(0px) rotate(-2deg); }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-content h1 {
  font-size: 3.5rem;
}

.lead {
  font-size: 1.1rem;
  color: #8a7e74;
}

.stats-section {
  padding: 60px 20px;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--color-primary);
}

.step-card {
  padding: 32px 24px;
  text-align: center;
  position: relative;
}

.step-num {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 32px;
  height: 32px;
  background-color: var(--color-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.9rem;
}

.step-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: var(--color-white);
  color: var(--color-primary);
}

.footer {
  background-color: #e9e5e1;
  padding: 40px 20px;
}

@media (max-width: 768px) {
  .hero-content h1 { font-size: 2.2rem; }
}
</style>
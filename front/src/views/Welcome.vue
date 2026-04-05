<template>
  <div class="welcome-container trae-theme" @mousemove="handleMouseMove" ref="containerRef">
    <!-- 极简背景：网格 + 鼠标跟随光晕 -->
    <div class="bg-decoration">
      <div class="grid-layer"></div>
      <!-- 鼠标跟随的大型渐变球 -->
      <div 
        class="mouse-glow" 
        :style="{ 
          left: `${mousePos.x}px`, 
          top: `${mousePos.y}px` 
        }"
      ></div>
      <!-- 静态装饰球，增加层次感 -->
      <div class="glow-orb orb-static-1"></div>
      <div class="glow-orb orb-static-2"></div>
    </div>

    <!-- 极简顶部导航 -->
    <nav class="top-nav fade-in">
      <div class="logo-area">
        <div class="logo-icon">
          <div class="inner-icon"></div>
        </div>
        <span class="logo-text">NBA AI ANALYTICS</span>
      </div>
      <div class="nav-links">
        <a href="#" class="nav-item">功能</a>
        <a href="https://www.kaggle.com/datasets/eoinamoore/historical-nba-data-and-player-box-scores" target="_blank" class="nav-item">数据源</a>
        <a href="#" class="nav-item">关于</a>
      </div>
    </nav>

    <!-- 核心英雄区 -->
    <main class="hero-section">
      <div class="content-wrapper">
        <div class="badge-container slide-up" style="--delay: 0.1s">
          <div class="glow-badge">
            <span class="badge-dot"></span>
            Spark 引擎驱动 · 实时预测
          </div>
        </div>
        
        <h1 class="hero-title slide-up" style="--delay: 0.2s">
          让每一份数据，<br />
          <span class="gradient-text">都触手可及。</span>
        </h1>
        
        <p class="hero-subtitle slide-up" style="--delay: 0.3s">
          NBA 球员价值分析系统，利用领先的 AI 算法深度挖掘历史数据，<br />
          为您提供精准的赛场趋势预测与全方位的球员战力评估。
        </p>

        <div class="action-group slide-up" style="--delay: 0.4s">
          <button class="btn-primary" @click="enterSystem">
            立即开始使用
            <el-icon class="icon-right"><Right /></el-icon>
          </button>
          <button class="btn-secondary" @click="learnMore">
            了解更多
          </button>
        </div>
      </div>

      <!-- 右侧悬浮卡片展示 (Trae 风格预览) -->
      <div class="hero-visual fade-in" style="--delay: 0.6s">
        <div class="visual-stack">
          <div class="glass-card card-main">
            <div class="card-header">
              <div class="dot red"></div>
              <div class="dot yellow"></div>
              <div class="dot green"></div>
            </div>
            <div class="card-body">
              <div class="skeleton-line title"></div>
              <div class="skeleton-grid">
                <div class="skeleton-item"></div>
                <div class="skeleton-item"></div>
                <div class="skeleton-item"></div>
              </div>
            </div>
          </div>
          <div class="glass-card card-sub-1">
            <div class="mini-chart"></div>
          </div>
          <div class="glass-card card-sub-2">
            <div class="status-indicator">
              <span class="pulse"></span>
              Analysis Active
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部信息 -->
    <footer class="hero-footer fade-in" style="--delay: 0.8s">
      <div class="footer-left">
        <div class="tech-stack">
          <span class="tech-item">VUE 3</span>
          <span class="tech-item">SPARK</span>
          <span class="tech-item">AI</span>
        </div>
      </div>
      <div class="footer-right">
        <span class="copyright">© 2026 NBA Analytics. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Right } from '@element-plus/icons-vue'

const router = useRouter()
const containerRef = ref<HTMLElement | null>(null)
const mousePos = reactive({ x: -500, y: -500 }) // 初始在屏幕外

const handleMouseMove = (e: MouseEvent) => {
  if (!containerRef.value) return
  // 平滑跟踪鼠标位置
  mousePos.x = e.clientX
  mousePos.y = e.clientY
}

const enterSystem = () => {
  router.push('/dashboard')
}

const learnMore = () => {
  router.push('/about')
}

</script>

<style scoped>
/* 核心容器 */
.welcome-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  background-color: #ffffff;
  color: #1a1a1a;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 装饰层 */
.bg-decoration {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  overflow: hidden;
}

.grid-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-image: 
    linear-gradient(rgba(0, 0, 0, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(circle at center, black, transparent 90%);
  opacity: 0.6;
  animation: gridDrift 18s linear infinite;
}

@keyframes gridDrift {
  0% { background-position: 0 0, 0 0; }
  100% { background-position: 60px 60px, -60px 60px; }
}

.mouse-glow {
  position: absolute;
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(82, 82, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: left 0.1s ease-out, top 0.1s ease-out;
  pointer-events: none;
  z-index: 3;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.2;
  z-index: 2;
}

.orb-static-1 {
  width: 400px;
  height: 400px;
  background: #5252ff;
  top: -100px;
  right: 10%;
}

.orb-static-2 {
  width: 500px;
  height: 500px;
  background: #ff4d4d;
  bottom: -150px;
  left: 5%;
  opacity: 0.1;
}

/* 顶部导航 */
.top-nav {
  position: relative;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 80px;
  backdrop-filter: blur(5px);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: #1a1a1a;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.inner-icon {
  width: 14px;
  height: 14px;
  background: #fff;
  border-radius: 2px;
}

.logo-text {
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 1px;
  color: #000;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.7);
  text-decoration: none;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #5252ff;
}

/* 英雄区 */
.hero-section {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  align-items: center;
  padding: 0 80px;
  gap: 40px;
}

.content-wrapper {
  flex: 1.2;
}

.glow-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px;
  background: rgba(82, 82, 255, 0.08);
  border: 1px solid rgba(82, 82, 255, 0.15);
  border-radius: 100px;
  font-size: 13px;
  color: #5252ff;
  margin-bottom: 32px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  background: #5252ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #5252ff;
}

.hero-title {
  font-size: 80px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -3px;
  margin-bottom: 32px;
  color: #000;
}

.gradient-text {
  background: linear-gradient(90deg, #000 0%, rgba(0, 0, 0, 0.7) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(0, 0, 0, 0.7);
  line-height: 1.6;
  margin-bottom: 48px;
  max-width: 600px;
}

.action-group {
  display: flex;
  gap: 16px;
}

.btn-primary {
  height: 56px;
  padding: 0 32px;
  background: #000;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
}

.btn-primary:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.btn-secondary {
  height: 56px;
  padding: 0 32px;
  background: rgba(0, 0, 0, 0.03);
  color: #000;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: rgba(0, 0, 0, 0.08);
}

/* 视觉部分 */
.hero-visual {
  flex: 0.8;
  display: flex;
  justify-content: center;
  align-items: center;
  perspective: 2000px;
}

.visual-stack {
  position: relative;
  width: 400px;
  height: 400px;
  transform: rotateY(-20deg) rotateX(10deg);
}

.glass-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 24px;
  backdrop-filter: blur(20px);
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.08);
}

.card-main {
  width: 400px;
  height: 300px;
  z-index: 2;
  padding: 24px;
}

.card-header {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
}

.dot { width: 10px; height: 10px; border-radius: 50%; opacity: 0.4; }
.red { background: #ff5f57; }
.yellow { background: #febc2e; }
.green { background: #28c840; }

.skeleton-line { height: 12px; background: rgba(0, 0, 0, 0.03); border-radius: 6px; margin-bottom: 20px; }
.skeleton-line.title { width: 60%; }
.skeleton-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.skeleton-item { height: 80px; background: rgba(0, 0, 0, 0.02); border-radius: 12px; }

.card-sub-1 {
  width: 200px;
  height: 150px;
  bottom: -40px;
  left: -40px;
  z-index: 3;
  padding: 20px;
}

.mini-chart {
  width: 100%;
  height: 100%;
  background: linear-gradient(0deg, rgba(82, 82, 255, 0.05), transparent);
  border-bottom: 2px solid #5252ff;
  border-radius: 0 0 4px 4px;
}

.card-sub-2 {
  width: 180px;
  height: 80px;
  top: -20px;
  right: -20px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.7);
}

.pulse {
  width: 8px;
  height: 8px;
  background: #28c840;
  border-radius: 50%;
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0% { transform: scale(0.8); box-shadow: 0 0 0 0 rgba(40, 200, 64, 0.4); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(40, 200, 64, 0); }
  100% { transform: scale(0.8); box-shadow: 0 0 0 0 rgba(40, 200, 64, 0); }
}

/* 动画系统 */
@keyframes traeFadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes traeSlideUp { from { opacity: 0; transform: translateY(40px); } to { opacity: 1; transform: translateY(0); } }

.fade-in { opacity: 0; animation: traeFadeIn 1.2s ease-out forwards; animation-delay: var(--delay, 0s); }
.slide-up { opacity: 0; animation: traeSlideUp 1s cubic-bezier(0.16, 1, 0.3, 1) forwards; animation-delay: var(--delay, 0s); }

/* 底部 */
.hero-footer {
  padding: 40px 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.tech-stack { display: flex; gap: 24px; }
.tech-item { font-size: 12px; color: rgba(0, 0, 0, 0.7); font-weight: 700; letter-spacing: 2px; }
.copyright { font-size: 12px; color: rgba(0, 0, 0, 0.8); }

.icon-right { margin-left: 8px; }

@media (max-width: 1200px) {
  .hero-title { font-size: 60px; }
  .hero-visual { display: none; }
  .hero-section { padding: 0 40px; }
  .top-nav { padding: 24px 40px; }
}
</style>

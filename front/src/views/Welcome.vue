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
      <!-- 静态装饰球，增加层次感：NBA红蓝配色 -->
      <div class="glow-orb orb-static-1"></div>
      <div class="glow-orb orb-static-2"></div>
      <!-- 球场底纹装饰 -->
      <div class="court-bg-marks"></div>
    </div>

    <!-- 极简顶部导航 -->
    <nav class="top-nav fade-in">
      <div class="logo-area">
        <div class="logo-icon">
          <span class="basketball-logo">🏀</span>
        </div>
        <span class="logo-text">NBA <span class="highlight-blue">AI</span> ANALYTICS</span>
      </div>
      <div class="nav-links">
        <a href="#" class="nav-item">功能体验</a>
        <a href="https://www.kaggle.com/datasets/eoinamoore/historical-nba-data-and-player-box-scores" target="_blank" class="nav-item">历史数据源</a>
        <a href="#" class="nav-item">技术架构</a>
      </div>
    </nav>

    <!-- 核心英雄区 -->
    <main class="hero-section">
      <div class="content-wrapper">
        <div class="badge-container slide-up" style="--delay: 0.1s">
          <div class="glow-badge">
            <span class="badge-dot"></span>
            Spark 引擎驱动 · 球场战况实时推演
          </div>
        </div>
        
        <h1 class="hero-title slide-up" style="--delay: 0.2s">
          洞悉赛场风云，<br />
          <span class="gradient-text">掌控制胜数据。</span>
        </h1>
        
        <p class="hero-subtitle slide-up" style="--delay: 0.3s">
          专为 NBA 赛场打造的数字分析枢纽，利用领先的 AI 集成算法挖掘海量战报数据。<br />
          为您呈现核心球员的全面战力评估，与未来巅峰对决的胜率走势。
        </p>

        <div class="action-group slide-up" style="--delay: 0.4s">
          <button class="btn-primary" @click="enterSystem">
            进入系统
            <el-icon class="icon-right"><Right /></el-icon>
          </button>
          <button class="btn-secondary" @click="learnMore">
            了解体系
          </button>
        </div>
      </div>

      <!-- 右侧悬浮卡片展示 (NBA 数据风) -->
      <div class="hero-visual fade-in" style="--delay: 0.6s">
        <div class="visual-stack">
          <div class="glass-card card-main">
            <transition name="slide-fade" mode="out-in">
              <div :key="currentPlayer.id">
                <div class="nba-card-header">
                  <div class="player-avatar-mock has-img" :style="currentPlayer.gradient">
                    <img :src="`https://cdn.nba.com/headshots/nba/latest/260x190/${currentPlayer.id}.png`" :alt="currentPlayer.name" class="player-img" />
                  </div>
                  <div class="player-info">
                    <div class="p-name">{{ currentPlayer.name }}</div>
                    <div class="p-team">{{ currentPlayer.team }}</div>
                  </div>
                  <div class="p-rating">{{ currentPlayer.ovr }}<span class="ovr">OVR</span></div>
                </div>
                <div class="nba-stats-grid">
                  <div class="stat-box">
                    <div class="val">{{ currentPlayer.pts }}</div>
                    <div class="lbl">PTS</div>
                  </div>
                  <div class="stat-box">
                    <div class="val">{{ currentPlayer.reb }}</div>
                    <div class="lbl">REB</div>
                  </div>
                  <div class="stat-box">
                    <div class="val">{{ currentPlayer.ast }}</div>
                    <div class="lbl">AST</div>
                  </div>
                </div>
              </div>
            </transition>
            <!-- 球场热区示意图 -->
            <div class="mock-court">
              <div class="court-paint"></div>
              <div class="court-arc"></div>
              <div class="hot-spot h-1"></div>
              <div class="hot-spot h-2"></div>
              <div class="hot-spot h-3"></div>
            </div>
          </div>
          <div class="glass-card card-sub-1">
            <div class="match-mini">
              <div class="teams"><span class="team-r">LAL</span> ⚔ <span class="team-b">GSW</span></div>
              <div class="prob-bar-bg">
                <div class="prob-bar-fill" style="width: 60%"></div>
                <div class="prob-marker">60%</div>
              </div>
              <div class="prob-desc">AI 胜率推演中...</div>
            </div>
          </div>
          <div class="glass-card card-sub-2">
            <div class="status-indicator">
              <span class="pulse"></span>
              Live Tracking Tracking Active
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部信息 -->
    <footer class="hero-footer fade-in" style="--delay: 0.8s">
      <div class="footer-left">
        <div class="tech-stack">
          <span class="tech-item"><span class="nba-color-dot red"></span>VUE 3</span>
          <span class="tech-item"><span class="nba-color-dot blue"></span>APACHE SPARK</span>
          <span class="tech-item"><span class="nba-color-dot black"></span>MACHINE LEARNING</span>
        </div>
      </div>
      <div class="footer-right">
        <span class="copyright">© 2026 NBA Analytics. Code the Court.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Right } from '@element-plus/icons-vue'

const router = useRouter()
const containerRef = ref<HTMLElement | null>(null)
const mousePos = reactive({ x: -500, y: -500 }) // 初始在屏幕外

// 动态球员数据展示
const showcasePlayers = [
  { id: 2544, name: 'L. James', team: 'LAL | SF', ovr: 96, pts: '25.7', reb: '7.3', ast: '8.3', gradient: 'background: linear-gradient(135deg, #fdb927 0%, #552583 100%)' }, // Lakers
  { id: 201939, name: 'S. Curry', team: 'GSW | PG', ovr: 95, pts: '26.4', reb: '4.5', ast: '5.1', gradient: 'background: linear-gradient(135deg, #006BB6 0%, #FDB927 100%)' }, // Warriors
  { id: 203999, name: 'N. Jokic', team: 'DEN | C', ovr: 98, pts: '26.4', reb: '12.4', ast: '9.0', gradient: 'background: linear-gradient(135deg, #0E2240 0%, #FEC524 100%)' }, // Nuggets
  { id: 1629029, name: 'L. Doncic', team: 'DAL | PG', ovr: 97, pts: '33.9', reb: '9.2', ast: '9.8', gradient: 'background: linear-gradient(135deg, #00538C 0%, #B8C4CA 100%)' }, // Mavs
  { id: 1628369, name: 'J. Tatum', team: 'BOS | SF', ovr: 95, pts: '26.9', reb: '8.1', ast: '4.9', gradient: 'background: linear-gradient(135deg, #007A33 0%, #BA9653 100%)' }, // Celtics
]
const currentPlayerIndex = ref(0)
const currentPlayer = computed(() => showcasePlayers[currentPlayerIndex.value])

let playerInterval: ReturnType<typeof setInterval>
onMounted(() => {
  playerInterval = setInterval(() => {
    currentPlayerIndex.value = (currentPlayerIndex.value + 1) % showcasePlayers.length
  }, 3500)
})

onUnmounted(() => {
  if (playerInterval) clearInterval(playerInterval)
})

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
  background: radial-gradient(circle, rgba(82, 82, 255, 0.3) 0%, rgba(64, 158, 255, 0.15) 30%, transparent 70%);
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
  background: #1D428A;
  top: -100px;
  right: 10%;
}

.orb-static-2 {
  width: 500px;
  height: 500px;
  background: #C9082A;
  bottom: -150px;
  left: 5%;
  opacity: 0.15;
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
  width: 38px;
  height: 38px;
  background: #1a1a1a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.basketball-logo {
  font-size: 22px;
  line-height: 1;
}

.logo-text {
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 1px;
  color: #000;
}

.highlight-blue {
  color: #1D428A;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  font-size: 14px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.7);
  text-decoration: none;
  transition: color 0.3s;
}

.nav-item:hover {
  color: #C9082A;
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
  background: rgba(201, 8, 42, 0.08); /* NBA Red alpha */
  border: 1px solid rgba(201, 8, 42, 0.15);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
  color: #C9082A;
  margin-bottom: 32px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  background: #C9082A;
  border-radius: 50%;
  box-shadow: 0 0 10px #C9082A;
}

.hero-title {
  font-size: 72px;
  font-weight: 900;
  line-height: 1.1;
  letter-spacing: -2px;
  margin-bottom: 32px;
  color: #1a1a1a;
}

.gradient-text {
  background: linear-gradient(90deg, #1D428A 0%, #C9082A 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(0, 0, 0, 0.7);
  font-weight: 500;
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
  background: linear-gradient(135deg, #1a1a1a 0%, #2d3748 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.btn-primary:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #C9082A 0%, #8b041b 100%);
}

.btn-secondary {
  height: 56px;
  padding: 0 32px;
  background: rgba(0, 0, 0, 0.03);
  color: #000;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: rgba(0, 0, 0, 0.08);
  border-color: #1D428A;
  color: #1D428A;
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
  transform: rotateY(-15deg) rotateX(10deg);
  transition: transform 0.5s ease;
}
.visual-stack:hover {
  transform: rotateY(-5deg) rotateX(5deg) scale(1.05);
}

.glass-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  backdrop-filter: blur(24px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.08), inset 0 0 0 1px rgba(255,255,255,0.5);
}

.card-main {
  width: 400px;
  height: 320px;
  z-index: 2;
  padding: 24px;
}

/* 球员卡样式 */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from {
  transform: translateX(15px);
  opacity: 0;
}
.slide-fade-leave-to {
  transform: translateX(-15px);
  opacity: 0;
}

.nba-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}
.player-avatar-mock {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.player-avatar-mock.has-img {
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.8);
}
.player-img {
  width: 140%;
  height: auto;
  object-fit: cover;
  transform: translateY(12px);
}
.player-info {
  flex: 1;
}
.p-name {
  font-weight: 900;
  font-size: 20px;
  color: #1a202c;
  letter-spacing: -0.5px;
}
.p-team {
  font-size: 14px;
  color: #718096;
  font-weight: 700;
  margin-top: 2px;
}
.p-rating {
  font-size: 32px;
  font-weight: 900;
  color: #1D428A;
  display: flex;
  align-items: baseline;
}
.p-rating .ovr {
  font-size: 12px;
  margin-left: 2px;
  color: #a0aec0;
  font-weight: 800;
}

.nba-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}
.stat-box {
  background: #f7fafc;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  border: 1px solid rgba(0,0,0,0.02);
}
.stat-box .val {
  font-size: 22px;
  font-weight: 900;
  color: #1a202c;
}
.stat-box .lbl {
  font-size: 12px;
  font-weight: 700;
  color: #a0aec0;
  margin-top: 2px;
}

/* 球场热区示意图 */
.mock-court {
  position: relative;
  height: 100px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fafc 0%, #edf2f7 100%);
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.court-paint {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 70px;
  border: 2px solid #cbd5e0;
  border-bottom: none;
  background: rgba(29, 66, 138, 0.05);
}
.court-arc {
  position: absolute;
  bottom: -40px;
  left: 50%;
  transform: translateX(-50%);
  width: 180px;
  height: 180px;
  border: 2px solid #cbd5e0;
  border-radius: 50%;
}
.hot-spot {
  position: absolute;
  width: 24px;
  height: 24px;
  background: radial-gradient(circle, rgba(201,8,42,0.7) 0%, transparent 60%);
  border-radius: 50%;
  animation: pulse-hotspot 2s infinite alternate;
}
@keyframes pulse-hotspot {
  0% { transform: scale(0.8); opacity: 0.8; }
  100% { transform: scale(1.2); opacity: 1; }
}
.h-1 { bottom: 10px; left: 40%; }
.h-2 { bottom: 40px; left: 60%; animation-delay: 0.5s; }
.h-3 { bottom: 25px; left: 75%; animation-delay: 1s; }

.card-sub-1 {
  width: 260px;
  height: 120px;
  bottom: -20px;
  left: -40px;
  z-index: 3;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.match-mini {
  width: 100%;
  padding: 16px 20px;
}
.teams {
  font-weight: 900;
  font-size: 16px;
  text-align: center;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}
.team-r { color: #C9082A; text-shadow: 0 2px 4px rgba(201,8,42,0.2); }
.team-b { color: #1D428A; text-shadow: 0 2px 4px rgba(29,66,138,0.2); }
.prob-bar-bg {
  height: 10px;
  background: rgba(0,0,0,0.05);
  border-radius: 5px;
  position: relative;
  overflow: hidden;
  margin-bottom: 8px;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
}
.prob-bar-fill {
  background: linear-gradient(90deg, #C9082A 0%, #1D428A 100%);
  height: 100%;
  transition: width 1s ease-in-out;
}
.prob-marker {
  position: absolute;
  right: 8px;
  top: -2px;
  font-size: 10px;
  font-weight: 900;
  color: white;
}
.prob-desc {
  font-size: 11px;
  color: #718096;
  text-align: center;
  font-weight: 600;
}

.card-sub-2 {
  width: 200px;
  height: 60px;
  top: -20px;
  right: -30px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.8);
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

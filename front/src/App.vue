<template>
  <el-container v-if="!isWelcomePage" class="layout-container trae-light-theme">
    <!-- 装饰背景 -->
    <div class="app-bg-decoration">
      <div class="grid-layer"></div>
    </div>

    <el-aside width="260px" class="sidebar">
      <div class="logo-container" @click="router.push('/')" style="cursor: pointer">
        <div class="logo-icon nba-primary-gradient">
          <span class="basketball-icon">🏀</span>
        </div>
        <div class="logo-text">NBA <span class="highlight-blue">AI</span></div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="side-menu nba-menu"
        background-color="transparent"
        text-color="#4a5568"
        active-text-color="#C9082A"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据仪表盘</span>
        </el-menu-item>
        
        <el-menu-item index="/players">
          <el-icon><User /></el-icon>
          <span>球员数据中心</span>
        </el-menu-item>
        
        <el-menu-item index="/teams">
          <el-icon><Trophy /></el-icon>
          <span>联盟球队数据</span>
        </el-menu-item>
        
        <el-menu-item index="/prediction/match">
          <el-icon><Flag /></el-icon>
          <span>比赛胜率推演</span>
        </el-menu-item>

        <el-menu-item index="/spark-analysis">
          <el-icon><Cpu /></el-icon>
          <span>Spark数据分析</span>
        </el-menu-item>

        <el-menu-item index="/about">
          <el-icon><InfoFilled /></el-icon>
          <span>关于项目架构</span>
        </el-menu-item>

        <div class="menu-divider"></div>
        
        <el-menu-item index="/admin">
          <el-icon><Setting /></el-icon>
          <span>系统状态与配置</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="version">Engine v3.0 (Spark)</div>
        <div class="copyright">© 2026 Code the Court.</div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="main-header glass-header">
        <div class="header-left">
          <div class="breadcrumb-nav">
            <span class="root-node" @click="router.push('/')">NBA数据分析系统</span>
            <span class="separator">/</span>
            <span class="current-node">{{ currentRouteName }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="status-badge nba-status">
            <span class="pulse-dot red"></span>
            Live Data Active
          </div>
        </div>
      </el-header>

      <el-main class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
  <div v-else class="welcome-view">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataBoard, User, Trophy, Setting, Cpu, Flag, InfoFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isWelcomePage = computed(() => route.path === '/')
const activeMenu = computed(() => route.path)

const currentRouteName = computed(() => {
  if (route.path.startsWith('/players/')) return '球员详细情报'
  const nameMap: Record<string, string> = {
    '/dashboard': '实时数据大盘',
    '/players': '核心球员档案库',
    '/teams': '联盟实力多维对比',
    '/prediction': '球员高阶表现预测',
    '/prediction/match': 'AI 比赛胜率推演中心',
    '/spark-analysis': 'Spark 并行离线计算',
    '/admin': '数据调度核心系统',
    '/about': '关于分析系统架构'
  }
  return nameMap[route.path] || '分析控制台'
})
</script>

<style>
:root {
  --trae-primary: #C9082A; /* NBA Red */
  --trae-secondary: #1D428A; /* NBA Blue */
  --trae-bg: #f8fafc;
  --trae-text: #1a202c;
  --trae-text-secondary: #718096;
  --trae-border: #e2e8f0;
  --trae-sidebar-width: 260px;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  background-color: var(--trae-bg);
  color: var(--trae-text);
  overflow-x: hidden;
}

#app { height: 100%; }

.layout-container.trae-light-theme {
  height: 100vh;
  position: relative;
  background-color: var(--trae-bg);
  background: linear-gradient(120deg, #f0f4f8 0%, #e3e9f0 100%);
}

/* 装饰背景 */
.app-bg-decoration {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.grid-layer {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(29, 66, 138, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(29, 66, 138, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  mask-image: radial-gradient(circle at center, black, transparent 90%);
}

/* 侧边栏重构 */
.sidebar {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  border-right: 1px solid rgba(255,255,255,0.6);
  box-shadow: 4px 0 24px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.logo-container {
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: transform 0.2s ease;
}
.logo-container:hover {
  transform: translateX(4px);
}

.logo-icon.nba-primary-gradient {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1D428A 0%, #C9082A 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.basketball-icon {
  font-size: 18px;
  line-height: 1;
}

.logo-text {
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 0.5px;
  color: #1a202c;
}
.highlight-blue {
  color: var(--trae-secondary);
}

/* 侧边栏菜单样式覆盖 */
.side-menu.nba-menu {
  border-right: none;
  background: transparent;
  flex: 1;
  padding: 0 12px;
}

.side-menu.nba-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  border-radius: 12px;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--trae-text-secondary);
  transition: all 0.3s ease;
}
.side-menu.nba-menu .el-menu-item:hover {
  background: rgba(29, 66, 138, 0.04);
  color: var(--trae-secondary);
  transform: translateX(4px);
}
.side-menu.nba-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(201, 8, 42, 0.08) 0%, transparent 100%);
  color: var(--trae-primary);
  font-weight: 800;
  border-left: 4px solid var(--trae-primary);
}
/* 侧边栏底盘与文字 */
.version { font-size: 12px; font-weight: 800; color: var(--trae-secondary); margin-bottom: 6px; }
.copyright { font-size: 11px; color: var(--trae-text-secondary); font-weight: 500; }

.menu-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
  margin: 16px 12px;
}

.sidebar-footer {
  padding: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

/* 主内容区 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: transparent;
}

/* 顶部 Header */
.main-header.glass-header {
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255,255,255,0.7);
  box-shadow: 0 4px 16px rgba(0,0,0,0.02);
  z-index: 10;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 600;
}

.root-node {
  color: var(--trae-text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.root-node:hover {
  color: var(--trae-primary);
}

.separator {
  color: #cbd5e0;
}

.current-node {
  color: var(--trae-text);
  font-weight: 800;
}

.status-badge.nba-status {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px;
  background: rgba(201, 8, 42, 0.05);
  border: 1px solid rgba(201, 8, 42, 0.1);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 700;
  color: var(--trae-primary);
}

.pulse-dot.red {
  width: 8px;
  height: 8px;
  background-color: var(--trae-primary);
  border-radius: 50%;
  animation: pulse-ring 2s infinite;
  box-shadow: 0 0 6px var(--trae-primary);
}

@keyframes pulse-ring {
  0% { transform: scale(0.8); box-shadow: 0 0 0 0 rgba(201, 8, 42, 0.4); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(201, 8, 42, 0); }
  100% { transform: scale(0.8); box-shadow: 0 0 0 0 rgba(201, 8, 42, 0); }
}

/* 页面内容区 */
.page-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  position: relative;
  z-index: 5;
}

/* 路由动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(16px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-16px);
}

.welcome-view { width: 100vw; height: 100vh; }
</style>

<template>
  <el-container v-if="!isWelcomePage" class="layout-container trae-light-theme">
    <!-- 装饰背景 -->
    <div class="app-bg-decoration">
      <div class="grid-layer"></div>
    </div>

    <el-aside width="260px" class="sidebar">
      <div class="logo-container" @click="router.push('/')" style="cursor: pointer">
        <div class="logo-icon">
          <div class="inner-icon"></div>
        </div>
        <div class="logo-text">NBA AI ANALYTICS</div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="side-menu"
        background-color="transparent"
        text-color="rgba(0, 0, 0, 0.6)"
        active-text-color="#5252ff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据仪表盘</span>
        </el-menu-item>
        
        <el-menu-item index="/players">
          <el-icon><User /></el-icon>
          <span>球员中心</span>
        </el-menu-item>
        
        <el-menu-item index="/teams">
          <el-icon><Trophy /></el-icon>
          <span>球队数据对比</span>
        </el-menu-item>
        
        <el-menu-item index="/prediction/match">
          <el-icon><Flag /></el-icon>
          <span>比赛结果预测</span>
        </el-menu-item>

        <el-menu-item index="/spark-analysis">
          <el-icon><Cpu /></el-icon>
          <span>Spark 离线分析</span>
        </el-menu-item>

        <div class="menu-divider"></div>
        
        <el-menu-item index="/admin">
          <el-icon><Setting /></el-icon>
          <span>后台数据管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="version">v3.0.0 Stable</div>
        <div class="copyright">© 2026 NBA AI</div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="main-header">
        <div class="header-left">
          <div class="breadcrumb-nav">
            <span class="root-node" @click="router.push('/')">首页</span>
            <span class="separator">/</span>
            <span class="current-node">{{ currentRouteName }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="status-badge">
            <span class="pulse-dot"></span>
            System Online
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
import { DataBoard, User, Trophy, Setting, Cpu, Flag } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isWelcomePage = computed(() => route.path === '/')
const activeMenu = computed(() => route.path)

const currentRouteName = computed(() => {
  if (route.path.startsWith('/players/')) return '球员详情'
  const nameMap: Record<string, string> = {
    '/dashboard': '数据仪表盘',
    '/players': '球员中心',
    '/teams': '球队数据对比',
    '/prediction': '球员表现预测',
    '/prediction/match': '比赛结果预测',
    '/spark-analysis': 'Spark 离线分析',
    '/admin': '后台数据管理',
    '/about': '关于项目'
  }
  return nameMap[route.path] || '系统页面'
})
</script>

<style>
:root {
  --trae-primary: #5252ff;
  --trae-bg: #ffffff;
  --trae-text: #1a1a1a;
  --trae-text-secondary: rgba(0, 0, 0, 0.6);
  --trae-border: rgba(0, 0, 0, 0.08);
  --trae-sidebar-width: 260px;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--trae-bg);
  color: var(--trae-text);
}

#app { height: 100%; }

.layout-container.trae-light-theme {
  height: 100vh;
  position: relative;
  background-color: #fcfcfc;
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
    linear-gradient(rgba(0, 0, 0, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(circle at center, black, transparent 90%);
}

/* 侧边栏重构 */
.sidebar {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-right: 1px solid var(--trae-border);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: all 0.3s;
}

.logo-container {
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 24px;
  height: 24px;
  background: #000;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.inner-icon {
  width: 10px;
  height: 10px;
  background: #fff;
  border-radius: 2px;
}

.logo-text {
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #000;
}

.side-menu {
  border-right: none !important;
  flex: 1;
  padding: 0 12px;
}

.el-menu-item, .el-sub-menu__title {
  height: 44px !important;
  line-height: 44px !important;
  margin: 4px 0 !important;
  border-radius: 10px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.el-menu-item:hover, .el-sub-menu__title:hover {
  background-color: rgba(0, 0, 0, 0.03) !important;
  color: #000 !important;
}

.el-menu-item.is-active {
  background-color: rgba(82, 82, 255, 0.08) !important;
  color: var(--trae-primary) !important;
  font-weight: 700 !important;
}

.el-menu-item .el-icon, .el-sub-menu__title .el-icon {
  font-size: 18px;
  margin-right: 10px;
}

.menu-divider {
  height: 1px;
  background: var(--trae-border);
  margin: 16px 12px;
}

.sidebar-footer {
  padding: 24px;
  border-top: 1px solid var(--trae-border);
}

.version { font-size: 11px; font-weight: 700; color: #000; letter-spacing: 1px; margin-bottom: 4px; }
.copyright { font-size: 10px; color: var(--trae-text-secondary); }

/* 主容器 */
.main-container {
  position: relative;
  z-index: 1;
  background: transparent;
}

.main-header {
  height: 72px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--trae-border);
}

/* 面包屑导航 */
.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.root-node { color: var(--trae-text-secondary); cursor: pointer; transition: color 0.2s; }
.root-node:hover { color: #000; }
.separator { color: rgba(0, 0, 0, 0.2); }
.current-node { color: #000; font-weight: 600; }

/* 状态标签 */
.status-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(40, 200, 64, 0.08);
  border: 1px solid rgba(40, 200, 64, 0.15);
  border-radius: 100px;
  font-size: 11px;
  font-weight: 700;
  color: #28c840;
}

.pulse-dot {
  width: 6px;
  height: 6px;
  background: #28c840;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(40, 200, 64, 0.5);
}

.page-content {
  padding: 40px !important;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

.welcome-view { width: 100vw; height: 100vh; }
</style>

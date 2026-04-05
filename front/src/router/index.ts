import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Welcome',
    component: () => import('../views/Welcome.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/players',
    name: 'PlayerAnalysis',
    component: () => import('../views/PlayerAnalysis.vue')
  },
  {
    path: '/players/:id',
    name: 'PlayerDetail',
    component: () => import('../views/PlayerDetail.vue')
  },
  {
    path: '/teams',
    name: 'TeamComparison',
    component: () => import('../views/TeamComparison.vue')
  },
  {
    path: '/prediction',
    name: 'Prediction',
    redirect: { path: '/players', query: { tab: 'predict' } }
  },
  {
    path: '/prediction/match',
    name: 'MatchPrediction',
    component: () => import('../views/MatchPrediction.vue')
  },
  {
    path: '/spark-analysis',
    name: 'SparkAnalysis',
    component: () => import('../views/SparkAnalysis.vue')
  },
  {
    path: '/games',
    name: 'GameRecords',
    component: () => import('../views/GameRecords.vue')
  },
  {
    path: '/rankings',
    name: 'PowerRankings',
    component: () => import('../views/PowerRankings.vue')
  },
  {
    path: '/admin',
    name: 'DataManagement',
    component: () => import('../views/DataManagement.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

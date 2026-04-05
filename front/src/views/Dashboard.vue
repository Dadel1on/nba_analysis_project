<template>
  <div class="dashboard-container fade-in" v-loading="loading">
    <div class="page-header slide-up">
      <div class="header-main">
        <h1>数据仪表盘</h1>
        <div class="status-badge">
          <span class="pulse-dot"></span>
          LIVE DATA
        </div>
      </div>
      <p class="header-subtitle">实时监控全联盟球员动态、核心表现指标及即时赛况</p>
    </div>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      closable
      class="error-alert"
    />

    <el-row :gutter="24" class="stat-row slide-up" style="--delay: 0.1s">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card">
          <div class="stat-icon-box" :style="{ color: stat.color, background: stat.bg }">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-trend sync">
              <span class="sync-dot"></span>
              实时同步
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-row slide-up" style="--delay: 0.15s">
      <el-col :span="16">
        <div class="glass-section">
          <div class="section-header">
            <div class="header-left">
              <el-icon class="title-icon"><TrendCharts /></el-icon>
              <h3>联盟得分趋势</h3>
            </div>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-section">
          <div class="section-header">
            <div class="header-left">
              <el-icon class="title-icon"><PieChart /></el-icon>
              <h3>球员位置分布</h3>
            </div>
          </div>
          <div ref="positionChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="content-row">
      <el-col :span="16">
        <div class="glass-section slide-up" style="--delay: 0.2s">
          <div class="section-header">
            <div class="header-left">
              <el-icon class="title-icon"><Trophy /></el-icon>
              <h3>球员得分榜单 (Top 10)</h3>
            </div>
            <el-button link class="view-all-btn" @click="router.push('/rankings')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          
          <el-table :data="rankings" class="custom-table" style="width: 100%">
            <el-table-column label="排名" width="80" align="center">
              <template #default="scope">
                <span :class="['rank-badge', `rank-${scope.$index + 1}`]">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="球员">
              <template #default="scope">
                <div class="player-info">
                  <el-avatar :size="32" class="player-avatar">{{ scope.row.avatarText }}</el-avatar>
                  <div class="player-name-group">
                    <span class="player-name">{{ scope.row.name }}</span>
                    <span class="player-team">{{ scope.row.team }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="points" label="场均得分" align="center" width="140">
              <template #default="scope">
                <div class="score-cell">{{ formatPoints(scope.row.points) }}</div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="right-stack slide-up" style="--delay: 0.3s">
          <div class="glass-section">
            <div class="section-header">
              <div class="header-left">
                <el-icon class="title-icon"><Calendar /></el-icon>
                <h3>近期比赛</h3>
              </div>
            </div>
            
            <div class="game-list">
              <div v-for="(game, index) in games" :key="index" class="game-item">
                <div class="game-time">{{ game.date }}</div>
                <div class="matchup">
                  <div class="team">
                    <span class="team-name">{{ game.awayTeam }}</span>
                  </div>
                  <div class="score-vs">
                    <span class="vs-text">VS</span>
                  </div>
                  <div class="team text-right">
                    <span class="team-name">{{ game.homeTeam }}</span>
                  </div>
                </div>
                <div class="game-footer">
                  <div class="prediction-tag">最终比分: {{ game.score }}</div>
                  <el-button size="small" round class="btn-detail" @click="router.push('/games')">详情</el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="glass-section">
            <div class="section-header">
              <div class="header-left">
                <el-icon class="title-icon"><User /></el-icon>
                <h3>本赛季得分王 (Top 5)</h3>
              </div>
              <el-button link class="view-all-btn" @click="router.push('/rankings')">
                查看榜单 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>

            <div class="top-player-list">
              <div v-for="(p, idx) in overviewTopPlayers" :key="idx" class="top-player-item">
                <div class="top-left">
                  <span class="mini-rank">{{ idx + 1 }}</span>
                  <div class="top-meta">
                    <div class="top-name">{{ p.name }}</div>
                    <div class="top-team">{{ p.team }}</div>
                  </div>
                </div>
                <div class="top-right">
                  <span class="top-points">{{ formatPoints(p.points) }}</span>
                  <span class="top-unit">PPG</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Timer, Trophy, ArrowRight, Calendar, TrendCharts, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardOverview, getGameRecords, getPowerRankings } from '@/api/dashboard'
import type { DashboardPayload, PositionDistributionItem, TopPlayer, TrendPoint } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const errorMessage = ref('')

type StatCard = {
  label: string
  value: string
  icon: unknown
  color: string
  bg: string
}

type RankingRow = {
  name: string
  team: string
  points: number
  avatarText: string
}

type GameRow = {
  date: string
  homeTeam: string
  awayTeam: string
  score: string
}

const stats = ref<StatCard[]>([
  { label: '现役球员', value: '-', icon: User, color: '#5252ff', bg: 'rgba(82, 82, 255, 0.08)' },
  { label: '联盟球队', value: '-', icon: Trophy, color: '#ff4d4d', bg: 'rgba(255, 77, 77, 0.08)' },
  { label: '赛季场次', value: '-', icon: Calendar, color: '#28c840', bg: 'rgba(40, 200, 64, 0.08)' },
  { label: '赛季总得分', value: '-', icon: Timer, color: '#febc2e', bg: 'rgba(254, 188, 46, 0.08)' },
])

const rankings = ref<RankingRow[]>([])
const games = ref<GameRow[]>([])
const trend = ref<TrendPoint[]>([])
const positionDistribution = ref<PositionDistributionItem[]>([])
const overviewTopPlayers = ref<TopPlayer[]>([])
const positionTotal = ref(0)

const trendChartRef = ref<HTMLElement | null>(null)
const positionChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let positionChart: echarts.ECharts | null = null

const toCompact = (num: number) => {
  if (!Number.isFinite(num)) return '-'
  return num.toLocaleString()
}

const buildAvatarText = (name: string) => {
  const trimmed = (name || '').trim()
  if (!trimmed) return '?'
  const parts = trimmed.split(/\s+/).filter(Boolean)
  const initials = parts.slice(0, 2).map((p) => p[0]?.toUpperCase()).filter(Boolean).join('')
  return initials || trimmed[0].toUpperCase()
}

const formatPoints = (points: number) => {
  if (!Number.isFinite(points)) return '-'
  return points.toFixed(1)
}

const applyOverview = (overview: DashboardPayload) => {
  stats.value = [
    { label: '现役球员', value: toCompact(overview.stats.activePlayers), icon: User, color: '#5252ff', bg: 'rgba(82, 82, 255, 0.08)' },
    { label: '联盟球队', value: toCompact(overview.stats.teams), icon: Trophy, color: '#ff4d4d', bg: 'rgba(255, 77, 77, 0.08)' },
    { label: '赛季场次', value: toCompact(overview.stats.seasonGames), icon: Calendar, color: '#28c840', bg: 'rgba(40, 200, 64, 0.08)' },
    { label: '赛季总得分', value: toCompact(overview.stats.predictions), icon: Timer, color: '#febc2e', bg: 'rgba(254, 188, 46, 0.08)' },
  ]
  trend.value = overview.trend || []
  positionDistribution.value = overview.positionDistribution || []
  overviewTopPlayers.value = overview.topPlayers || []
  positionTotal.value = positionDistribution.value.reduce((sum, i) => sum + (Number(i.value) || 0), 0)
}

const computeMA = (arr: number[], w = 3) => {
  return arr.map((_, i) => {
    if (i + 1 < w) return null
    const slice = arr.slice(i - w + 1, i + 1)
    const avg = slice.reduce((a, b) => a + b, 0) / w
    return Number(avg.toFixed(1))
  })
}

const initOrUpdateCharts = () => {
  if (trendChartRef.value) {
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }
    const xData = trend.value.map((p) => p.season)
    const yData = trend.value.map((p) => p.avgPoints)
    const minY = Math.min(...yData)
    const maxY = Math.max(...yData)
    const pad = Math.max(1, (maxY - minY) * 0.15)
    const ma3 = computeMA(yData, 3)
    trendChart.setOption(
      {
        backgroundColor: 'transparent',
        grid: { left: 10, right: 10, top: 22, bottom: 45, containLabel: true },
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderColor: 'rgba(0,0,0,0.15)',
          textStyle: { color: 'rgba(0,0,0,0.7)' },
          formatter: (items: any) => {
            const title = items[0]?.axisValueLabel || ''
            const lines = items
              .map((it: any) => {
                const val = it.data == null ? '-' : it.data
                return `<div><span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${it.color};"></span>${it.seriesName}<span style="float:right;font-weight:900">${val}</span></div>`
              })
              .join('')
            return `<div style="font-weight:700;margin-bottom:6px">${title}</div>${lines}`
          },
        },
        dataZoom: [
          { type: 'inside', xAxisIndex: 0 },
          {
            type: 'slider',
            xAxisIndex: 0,
            height: 8,
            bottom: 28,
            brushSelect: false,
            handleSize: 0,
            showDetail: false,
            showDataShadow: false,
            borderColor: 'rgba(0,0,0,0)',
            fillerColor: 'rgba(82, 82, 255, 0.14)',
            backgroundColor: 'rgba(0, 0, 0, 0.04)',
            dataBackground: {
              lineStyle: { opacity: 0 },
              areaStyle: { opacity: 0 },
            },
            selectedDataBackground: {
              lineStyle: { opacity: 0 },
              areaStyle: { opacity: 0 },
            },
          },
        ],
        xAxis: {
          type: 'category',
          data: xData,
          axisLine: { lineStyle: { color: 'rgba(0,0,0,0.08)' } },
          axisLabel: { color: 'rgba(0,0,0,0.55)', fontWeight: 600, fontSize: 11, hideOverlap: true, margin: 10 },
          axisTick: { show: false },
        },
        yAxis: {
          type: 'value',
           min: Math.floor(minY - pad),
           max: Math.ceil(maxY + pad),
           scale: true,
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { color: 'rgba(0,0,0,0.45)', fontWeight: 600 },
          splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)', type: 'dashed' } },
        },
        series: [
          {
            name: '场均得分',
            type: 'line',
            smooth: 0.25,
            data: yData,
            symbol: 'circle',
            symbolSize: 7,
            lineStyle: { width: 3, color: '#5252ff' },
            itemStyle: { color: '#5252ff' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(82, 82, 255, 0.18)' },
                { offset: 1, color: 'rgba(82, 82, 255, 0.00)' },
              ]),
            },
            markPoint: {
              symbol: 'pin',
              symbolSize: 36,
              itemStyle: { color: '#5252ff' },
              label: {
                position: 'top',
                distance: 8,
                color: 'rgba(0,0,0,0.85)',
                fontWeight: 900,
                fontSize: 11,
                backgroundColor: 'rgba(255,255,255,0.95)',
                borderColor: 'rgba(0,0,0,0.12)',
                borderWidth: 1,
                borderRadius: 10,
                padding: [4, 8],
                formatter: (p: any) => {
                  const v = Number(p?.value)
                  const valueText = Number.isFinite(v) ? v.toFixed(1) : '-'
                  const nameText = p?.name === '最高' ? '最高' : '最低'
                  return `${nameText} ${valueText}`
                },
              },
              data: [{ type: 'max', name: '最高' }, { type: 'min', name: '最低' }],
            },
            markLine: {
              lineStyle: { type: 'dashed', color: 'rgba(0,0,0,0.35)' },
              label: { color: 'rgba(0,0,0,0.65)', fontWeight: 800 },
              data: [{ type: 'average', name: '平均' }],
            },
            emphasis: { focus: 'series' },
            animationDuration: 600,
          },
          {
            name: '移动平均(3)',
            type: 'line',
            smooth: 0.25,
            data: ma3,
            symbol: 'none',
            lineStyle: { width: 2, type: 'dashed', color: '#7a7aff' },
            itemStyle: { color: '#7a7aff' },
            emphasis: { focus: 'series' },
            connectNulls: true,
          },
        ],
      },
      true,
    )
  }

  if (positionChartRef.value) {
    if (!positionChart) {
      positionChart = echarts.init(positionChartRef.value)
    }
    const data = positionDistribution.value.map((i) => ({ name: i.name, value: i.value }))
    positionChart.setOption(
      {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item' },
        graphic: [
          {
            type: 'text',
            left: 'center',
            top: 'center',
            style: {
              text: `${positionTotal.value}\n球员`,
              fill: 'rgba(0,0,0,0.65)',
              fontSize: 16,
              fontWeight: 900,
              textAlign: 'center',
            },
          },
        ],
        legend: {
          bottom: 0,
          left: 'center',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: { color: 'rgba(0,0,0,0.55)', fontWeight: 600 },
        },
        series: [
          {
            name: '位置分布',
            type: 'pie',
            radius: ['54%', '78%'],
            center: ['50%', '45%'],
            label: { show: false },
            labelLine: { show: false },
            itemStyle: { borderRadius: 10, borderColor: 'rgba(255,255,255,0.9)', borderWidth: 2 },
            color: ['#5252ff', '#ff4d4d', '#28c840', '#febc2e', '#7a7aff', '#909399'],
            data,
            emphasis: { scale: true, scaleSize: 6 },
            animationDuration: 600,
          },
        ],
      },
      true,
    )
  }
}

const resizeCharts = () => {
  trendChart?.resize()
  positionChart?.resize()
}

const loadDashboard = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const [overview, rankingsRes, gamesRes] = await Promise.all([
      getDashboardOverview(),
      getPowerRankings(1, 10),
      getGameRecords(1, 3),
    ])

    applyOverview(overview)

    rankings.value = rankingsRes.list.map((p) => ({
      name: p.name,
      team: p.team,
      points: p.points,
      avatarText: buildAvatarText(p.name),
    }))

    games.value = gamesRes.list.map((g) => ({
      date: g.date,
      homeTeam: g.homeTeam,
      awayTeam: g.awayTeam,
      score: g.score,
    }))

    await nextTick()
    initOrUpdateCharts()
  } catch (e) {
    errorMessage.value = e instanceof Error ? e.message : '加载仪表盘数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', resizeCharts, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  positionChart?.dispose()
  trendChart = null
  positionChart = null
})
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

.error-alert {
  margin-bottom: 20px;
  border-radius: 10px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

h1 {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -1px;
  margin: 0;
  color: #000;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
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
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(40, 200, 64, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(40, 200, 64, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(40, 200, 64, 0); }
}

.header-subtitle {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

/* 统计卡片 */
.stat-card {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.04);
  border-color: rgba(0, 0, 0, 0.1);
}

.stat-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.4);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #000;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 700;
}

.stat-trend.sync {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.5);
}

.sync-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #28c840;
  box-shadow: 0 0 10px rgba(40, 200, 64, 0.35);
}

/* 内容区块 */
.glass-section {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 24px;
  padding: 24px;
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.right-stack {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 20px;
  color: #5252ff;
}

h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #000;
}

.view-all-btn {
  font-size: 13px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.4);
}

.view-all-btn:hover {
  color: #5252ff;
}

/* 表格定制 */
.custom-table :deep(.el-table__header) {
  background-color: transparent !important;
}

.custom-table :deep(th.el-table__cell) {
  background-color: transparent !important;
  color: rgba(0, 0, 0, 0.4) !important;
  font-weight: 700 !important;
  font-size: 12px !important;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04) !important;
}

.custom-table :deep(.el-table__row) {
  background-color: transparent !important;
}

.custom-table :deep(.el-table__cell) {
  padding: 16px 0 !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04) !important;
}

.rank-badge {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 800;
  background: rgba(0, 0, 0, 0.03);
  color: rgba(0, 0, 0, 0.4);
}

.rank-1 { background: #febc2e; color: #fff; }
.rank-2 { background: #b8c0cc; color: #fff; }
.rank-3 { background: #cd7f32; color: #fff; }

.player-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.player-avatar {
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.7);
  font-weight: 800;
}

.player-name-group {
  display: flex;
  flex-direction: column;
}

.player-name {
  font-size: 14px;
  font-weight: 700;
  color: #000;
}

.player-team {
  font-size: 11px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.3);
}

.score-cell {
  font-weight: 800;
  color: #000;
}

.top-player-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.top-player-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 12px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.mini-rank {
  width: 22px;
  height: 22px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 900;
  background: rgba(82, 82, 255, 0.1);
  color: #5252ff;
}

.top-meta {
  min-width: 0;
}

.top-name {
  font-size: 13px;
  font-weight: 800;
  color: #000;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top-team {
  font-size: 11px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.4);
}

.top-right {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.top-points {
  font-size: 14px;
  font-weight: 900;
  color: #5252ff;
}

.top-unit {
  font-size: 10px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.35);
  letter-spacing: 1px;
}

/* 赛程列表 */
.game-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.game-item {
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 16px;
  padding: 20px;
}

.game-time {
  font-size: 11px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.3);
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.matchup {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.team {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.team.text-right { align-items: flex-end; }

.team-name {
  font-size: 15px;
  font-weight: 800;
  color: #000;
}

.team-record {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.4);
}

.vs-text {
  font-size: 12px;
  font-weight: 900;
  color: rgba(0, 0, 0, 0.1);
  padding: 0 16px;
}

.game-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.prediction-tag {
  font-size: 11px;
  font-weight: 700;
  color: #5252ff;
}

.btn-detail {
  font-size: 11px;
  font-weight: 700;
}

/* 动画 */
.fade-in { animation: fadeIn 0.8s ease-out forwards; }
.slide-up { opacity: 0; animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards; animation-delay: var(--delay, 0s); }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 1200px) {
  .el-col-6 { width: 50% !important; margin-bottom: 24px; }
  .el-col-16, .el-col-8 { width: 100% !important; }
}
</style>

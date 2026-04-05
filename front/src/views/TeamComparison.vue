<template>
  <div class="team-comparison-container">
    <div class="page-header">
      <h1>球队数据对比</h1>
      <p class="subtitle">深度横向对比各支球队的核心赛季指标与表现差异</p>
    </div>

    <!-- 对比配置面板 -->
    <el-card shadow="hover" class="compare-config-card">
      <div class="config-grid">
        <div class="team-selector home">
          <label>球队 A</label>
          <el-select
            v-model="selectedTeamA"
            placeholder="选择球队 A"
            filterable
            clearable
            :loading="loading"
            class="custom-select"
          >
            <template #prefix>
              <el-icon><Trophy /></el-icon>
            </template>
            <el-option
              v-for="team in teamNames"
              :key="`a-${team}`"
              :label="team"
              :value="team"
            />
          </el-select>
        </div>

        <div class="vs-badge">VS</div>

        <div class="team-selector away">
          <label>球队 B</label>
          <el-select
            v-model="selectedTeamB"
            placeholder="选择球队 B"
            filterable
            clearable
            :loading="loading"
            class="custom-select"
          >
            <template #prefix>
              <el-icon><Trophy /></el-icon>
            </template>
            <el-option
              v-for="team in teamNames"
              :key="`b-${team}`"
              :label="team"
              :value="team"
              :disabled="team === selectedTeamA"
            />
          </el-select>
        </div>

        <div class="filter-group">
          <div class="filter-item">
            <label>对比赛季</label>
            <el-select v-model="selectedSeason" class="season-select">
              <el-option v-for="year in seasonOptions" :key="year" :label="`${year} 赛季`" :value="year" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>对比维度</label>
            <el-segmented
              v-model="selectedMetric"
              :options="metricOptions"
              size="default"
              class="custom-segmented"
            />
          </div>
        </div>

        <div class="action-box">
          <el-button 
            type="primary" 
            size="large"
            :loading="comparing" 
            @click="runComparison"
            icon="Connection"
            class="compare-btn"
          >
            执行深度对比
          </el-button>
        </div>
      </div>
    </el-card>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      closable
      class="error-alert"
    />

    <!-- 对比结果展示 -->
    <el-row :gutter="20" v-if="comparison" class="result-row">
      <!-- 图表卡片 -->
      <el-col :span="24" :lg="16">
        <el-card shadow="always" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Histogram /></el-icon> 核心指标量化分析
              </span>
              <el-tag type="success" effect="plain" round size="small">可视化视图</el-tag>
            </div>
          </template>
          <div ref="compareChartRef" class="comparison-chart"></div>
        </el-card>
      </el-col>

      <!-- 数据摘要表格 -->
      <el-col :span="24" :lg="8">
        <el-card shadow="always" class="summary-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><List /></el-icon> 指标明细对照
              </span>
            </div>
          </template>
          <el-table
            :data="comparison.teams"
            style="width: 100%"
            class="summary-table"
          >
            <el-table-column prop="team" label="球队" min-width="100" />
            <el-table-column label="核心指标" align="center">
              <el-table-column prop="points" label="得分" width="70" />
              <el-table-column prop="rebounds" label="篮板" width="70" />
              <el-table-column prop="assists" label="助攻" width="70" />
            </el-table-column>
          </el-table>
          <div class="win-rate-summary" v-for="team in comparison.teams" :key="team.team">
            <div class="wr-label">{{ team.team }} 胜率</div>
            <el-progress 
              :percentage="Math.round(team.winRate * 100)" 
              :color="team === comparison.teams[0] ? '#C9082A' : '#006BB6'"
              :stroke-width="12"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待选球队列表 -->
    <el-card shadow="never" class="team-list-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><Grid /></el-icon> 候选球队名录
          </span>
          <span class="header-tip">可快速将球队加入对比槽位</span>
        </div>
      </template>
      <el-table :data="teams" style="width: 100%" stripe v-loading="loading" class="team-list-table">
        <el-table-column prop="name" label="球队名称" min-width="150">
          <template #default="scope">
            <span class="team-name-tag">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="150" />
        <el-table-column prop="conference" label="分区" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.conference === 'East' ? 'danger' : 'primary'" size="small">
              {{ scope.row.conference === 'East' ? '东部' : '西部' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              plain
              :disabled="selectedTeamA === scope.row.name"
              @click="useAsTeamA(scope.row.name)"
            >
              设为球队A
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              plain
              :disabled="selectedTeamB === scope.row.name"
              @click="useAsTeamB(scope.row.name)"
            >
              设为球队B
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !teams.length" description="未找到球队数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Trophy, Histogram, List, Grid, Connection } from '@element-plus/icons-vue'
import { compareTeams, getTeams } from '@/api/teams'
import type { TeamComparisonResult, TeamSummary } from '@/api/types'

const teams = ref<TeamSummary[]>([])
const loading = ref(false)
const comparing = ref(false)
const errorMessage = ref('')

const teamNames = ref<string[]>([])
const selectedTeamA = ref('')
const selectedTeamB = ref('')
const selectedSeason = ref(new Date().getFullYear() - 1)
const selectedMetric = ref<'all' | 'offense' | 'defense'>('all')
const comparison = ref<TeamComparisonResult | null>(null)
const compareChartRef = ref<HTMLElement | null>(null)
let compareChart: echarts.ECharts | null = null

const seasonOptions = Array.from({ length: 8 }, (_, idx) => new Date().getFullYear() - idx - 1)
const metricOptions = [
  { label: '综合', value: 'all' },
  { label: '进攻', value: 'offense' },
  { label: '防守', value: 'defense' },
]

const fetchTeams = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    teams.value = await getTeams()
    teamNames.value = Array.from(new Set(teams.value.map((item) => item.name).filter(Boolean)))
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to fetch teams'
    teams.value = []
    teamNames.value = []
  } finally {
    loading.value = false
  }
}

const useAsTeamA = (name: string) => {
  if (selectedTeamB.value === name) selectedTeamB.value = ''
  selectedTeamA.value = name
}

const useAsTeamB = (name: string) => {
  if (selectedTeamA.value === name) selectedTeamA.value = ''
  selectedTeamB.value = name
}

const runComparison = async () => {
  if (!selectedTeamA.value || !selectedTeamB.value) {
    errorMessage.value = '请先选择两支球队'
    return
  }
  if (selectedTeamA.value === selectedTeamB.value) {
    errorMessage.value = '请选择两支不同的球队'
    return
  }

  comparing.value = true
  errorMessage.value = ''
  try {
    comparison.value = await compareTeams(selectedTeamA.value, selectedTeamB.value, {
      season: selectedSeason.value,
      metric: selectedMetric.value,
    })
    await nextTick()
    renderCompareChart()
  } catch (error) {
    comparison.value = null
    errorMessage.value = error instanceof Error ? error.message : 'Team comparison failed'
  } finally {
    comparing.value = false
  }
}

const renderCompareChart = () => {
  if (!compareChartRef.value || !comparison.value?.teams?.length) return

  const [teamA, teamB] = comparison.value.teams
  compareChart?.dispose()
  compareChart = echarts.init(compareChartRef.value)
  
  const categories = chartCategories(selectedMetric.value)
  const dataA = chartValues(teamA, selectedMetric.value)
  const dataB = chartValues(teamB, selectedMetric.value)

  compareChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderWidth: 0,
      shadowBlur: 10,
      shadowColor: 'rgba(0,0,0,0.1)',
      textStyle: { color: '#333' },
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: [teamA.team, teamB.team],
      bottom: '0',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#666' }
    },
    grid: {
      top: '10%',
      left: '3%',
      right: '4%',
      bottom: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#eee' } },
      axisLabel: { color: '#999' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } },
      axisLabel: { color: '#999' }
    },
    series: [
      {
        name: teamA.team,
        type: 'bar',
        barWidth: '25%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ff4d4f' },
            { offset: 1, color: '#C9082A' }
          ])
        },
        data: dataA,
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      },
      {
        name: teamB.team,
        type: 'bar',
        barWidth: '25%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#40a9ff' },
            { offset: 1, color: '#006BB6' }
          ])
        },
        data: dataB,
        animationDuration: 1000,
        animationEasing: 'cubicOut'
      }
    ]
  })
}

function chartCategories(metric: 'all' | 'offense' | 'defense'): string[] {
  if (metric === 'offense') return ['场均得分', '场均助攻', '胜率(%)']
  if (metric === 'defense') return ['场均篮板', '赛季胜场', '胜率(%)']
  return ['得分', '篮板', '助攻', '胜场', '胜率(%)']
}

function chartValues(team: TeamComparisonResult['teams'][number], metric: 'all' | 'offense' | 'defense'): number[] {
  if (metric === 'offense') return [team.points, team.assists, Math.round(team.winRate * 100)]
  if (metric === 'defense') return [team.rebounds, team.wins, Math.round(team.winRate * 100)]
  return [team.points, team.rebounds, team.assists, team.wins, Math.round(team.winRate * 100)]
}

const handleResize = () => {
  compareChart?.resize()
}

onMounted(() => {
  fetchTeams()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  compareChart?.dispose()
})
</script>

<style scoped>
.team-comparison-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #909399;
  font-size: 15px;
}

/* 配置卡片 */
.compare-config-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;
}

.config-grid {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  flex-wrap: wrap;
}

.team-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 240px;
}

.team-selector label, .filter-item label {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.vs-badge {
  font-size: 20px;
  font-weight: 900;
  font-style: italic;
  color: #dcdfe6;
  margin-bottom: 8px;
}

.filter-group {
  display: flex;
  gap: 24px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.season-select {
  width: 160px;
}

.custom-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 6px 12px;
}

.compare-btn {
  border-radius: 12px;
  padding: 12px 28px;
  font-weight: 600;
}

/* 结果区域 */
.result-row {
  margin-bottom: 24px;
}

.chart-card, .summary-card {
  border-radius: 16px;
  border: none;
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comparison-chart {
  height: 380px;
  width: 100%;
}

.summary-table {
  margin-bottom: 20px;
}

.win-rate-summary {
  margin-top: 16px;
}

.wr-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

/* 列表卡片 */
.team-list-card {
  border-radius: 16px;
  border: none;
}

.header-tip {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.team-name-tag {
  font-weight: 600;
  color: #303133;
}

.team-list-table :deep(.el-table__row) {
  cursor: default;
}

.error-alert {
  margin-bottom: 20px;
  border-radius: 10px;
}
</style>

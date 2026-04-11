<template>
  <div class="team-comparison-container">
    <div class="page-header">
      <h1>球队数据对比</h1>
      <p class="subtitle">深度横向对比各支球队的核心赛季指标与表现差异</p>
    </div>

    <div class="overview-grid">
      <div class="overview-card">
        <div class="overview-label">已加载球队</div>
        <div class="overview-value">{{ teams.length }}</div>
      </div>
      <div class="overview-card east">
        <div class="overview-label">东部球队</div>
        <div class="overview-value">{{ eastCount }}</div>
      </div>
      <div class="overview-card west">
        <div class="overview-label">西部球队</div>
        <div class="overview-value">{{ westCount }}</div>
      </div>
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
          <div class="quick-actions">
            <el-button plain @click="swapTeams">交换球队</el-button>
            <el-button plain type="info" :disabled="teamNames.length < 2" @click="randomCompare">随机一组</el-button>
          </div>
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
      <el-col :span="24" :lg="14">
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
      <el-col :span="24" :lg="10">
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
            size="large"
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
              :stroke-width="14"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" v-if="comparison && duelMetrics.length" class="deep-insight-row">
      <el-col :span="24" :lg="8">
        <el-card shadow="always" class="battle-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Connection /></el-icon> 深度结论
              </span>
              <el-tag type="danger" effect="plain" round size="small">{{ selectedSeason }} 赛季</el-tag>
            </div>
          </template>
          <div class="battle-winner">{{ overallLeader }}</div>
          <p class="battle-desc">{{ deepConclusionSummary }}</p>
          <ul class="battle-points">
            <li v-for="point in deepConclusionPoints" :key="point">{{ point }}</li>
          </ul>
          <div class="battle-tags">
            <el-tag round size="small" effect="plain">维度：{{ metricText }}</el-tag>
            <el-tag round size="small" type="info" effect="plain">对抗指标：{{ duelMetrics.length }} 项</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" :lg="16">
        <el-card shadow="always" class="duel-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Histogram /></el-icon> 指标对抗分解
              </span>
            </div>
          </template>

          <div class="duel-list">
            <div class="duel-item" v-for="item in duelMetrics" :key="item.metric">
              <div class="duel-top">
                <span class="duel-metric">{{ item.metric }}</span>
                <span class="duel-leader">领先：{{ item.leader }}</span>
              </div>

              <div class="duel-progress-row">
                <span class="duel-team-label team-a">{{ comparison.teams[0].team }}</span>
                <el-progress :percentage="item.aPct" :show-text="false" :stroke-width="12" color="#C9082A" class="duel-progress" />
                <span class="duel-value">{{ item.aDisplay }}</span>
              </div>
              <div class="duel-progress-row">
                <span class="duel-team-label team-b">{{ comparison.teams[1].team }}</span>
                <el-progress :percentage="item.bPct" :show-text="false" :stroke-width="12" color="#006BB6" class="duel-progress" />
                <span class="duel-value">{{ item.bDisplay }}</span>
              </div>
              <div class="duel-diff">差值：{{ item.diffDisplay }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="comparisonLeaders.length" shadow="always" class="insight-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><Connection /></el-icon> 对比结论速览
          </span>
          <el-tag type="warning" effect="plain" round size="small">自动生成</el-tag>
        </div>
      </template>
      <div class="insight-grid">
        <div v-for="item in comparisonLeaders" :key="item.metric" class="insight-item">
          <div class="insight-metric">{{ item.metric }}</div>
          <div class="insight-team">领先: {{ item.winner }}</div>
          <div class="insight-diff">差值 {{ item.diff }}{{ item.unit }}</div>
        </div>
      </div>
    </el-card>

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
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
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

const eastCount = computed(() => teams.value.filter((team) => team.conference === 'East').length)
const westCount = computed(() => teams.value.filter((team) => team.conference === 'West').length)
const metricText = computed(() => {
  if (selectedMetric.value === 'offense') return '进攻'
  if (selectedMetric.value === 'defense') return '防守'
  return '综合'
})
const comparisonLeaders = computed(() => {
  if (!comparison.value || comparison.value.teams.length < 2) {
    return []
  }
  const [teamA, teamB] = comparison.value.teams
  const metrics = [
    { label: '得分', key: 'points' as const, unit: '分' },
    { label: '篮板', key: 'rebounds' as const, unit: '板' },
    { label: '助攻', key: 'assists' as const, unit: '次' },
    { label: '胜场', key: 'wins' as const, unit: '场' },
    { label: '胜率', key: 'winRate' as const, unit: '%' },
  ]
  return metrics.map((item) => {
    const valueA = item.key === 'winRate' ? Math.round(teamA[item.key] * 100) : teamA[item.key]
    const valueB = item.key === 'winRate' ? Math.round(teamB[item.key] * 100) : teamB[item.key]
    const winner = valueA === valueB ? '平局' : valueA > valueB ? teamA.team : teamB.team
    const diff = Math.abs(valueA - valueB)
    return {
      metric: item.label,
      winner,
      diff,
      unit: item.unit,
    }
  })
})

const seasonOptions = Array.from({ length: 8 }, (_, idx) => new Date().getFullYear() - idx - 1)
const metricOptions = [
  { label: '综合', value: 'all' },
  { label: '进攻', value: 'offense' },
  { label: '防守', value: 'defense' },
]

type MetricKey = 'points' | 'rebounds' | 'assists' | 'wins' | 'winRate'
type MetricDescriptor = {
  label: string
  key: MetricKey
  unit: string
  percentage?: boolean
}

const metricDescriptors: Record<'all' | 'offense' | 'defense', MetricDescriptor[]> = {
  all: [
    { label: '得分', key: 'points', unit: '分' },
    { label: '篮板', key: 'rebounds', unit: '板' },
    { label: '助攻', key: 'assists', unit: '次' },
    { label: '胜场', key: 'wins', unit: '场' },
    { label: '胜率', key: 'winRate', unit: '%', percentage: true },
  ],
  offense: [
    { label: '得分', key: 'points', unit: '分' },
    { label: '助攻', key: 'assists', unit: '次' },
    { label: '胜率', key: 'winRate', unit: '%', percentage: true },
  ],
  defense: [
    { label: '篮板', key: 'rebounds', unit: '板' },
    { label: '胜场', key: 'wins', unit: '场' },
    { label: '胜率', key: 'winRate', unit: '%', percentage: true },
  ],
}

function normalizeMetricValue(team: TeamComparisonResult['teams'][number], descriptor: MetricDescriptor): number {
  const raw = team[descriptor.key]
  if (descriptor.percentage) {
    return Number((raw * 100).toFixed(1))
  }
  return Number(raw.toFixed(1))
}

function formatMetricDisplay(value: number, unit: string): string {
  const text = Number.isInteger(value) ? String(value) : value.toFixed(1)
  return `${text}${unit}`
}

const duelMetrics = computed(() => {
  if (!comparison.value || comparison.value.teams.length < 2) {
    return []
  }

  const [teamA, teamB] = comparison.value.teams
  const descriptors = metricDescriptors[selectedMetric.value]
  return descriptors.map((descriptor) => {
    const aValue = normalizeMetricValue(teamA, descriptor)
    const bValue = normalizeMetricValue(teamB, descriptor)
    const maxValue = Math.max(aValue, bValue, 1)
    const aPct = Math.round((aValue / maxValue) * 100)
    const bPct = Math.round((bValue / maxValue) * 100)
    const leader = aValue === bValue ? '平局' : aValue > bValue ? teamA.team : teamB.team

    return {
      metric: descriptor.label,
      aPct,
      bPct,
      leader,
      aDisplay: formatMetricDisplay(aValue, descriptor.unit),
      bDisplay: formatMetricDisplay(bValue, descriptor.unit),
      diffValue: Number(Math.abs(aValue - bValue).toFixed(1)),
      diffDisplay: formatMetricDisplay(Math.abs(aValue - bValue), descriptor.unit),
    }
  })
})

const leadBreakdown = computed(() => {
  if (!comparison.value || comparison.value.teams.length < 2 || !duelMetrics.value.length) {
    return null
  }

  const [teamA, teamB] = comparison.value.teams
  let aLead = 0
  let bLead = 0
  let ties = 0
  duelMetrics.value.forEach((item) => {
    if (item.leader === teamA.team) aLead += 1
    else if (item.leader === teamB.team) bLead += 1
    else ties += 1
  })

  return {
    teamA,
    teamB,
    aLead,
    bLead,
    ties,
  }
})

const overallLeader = computed(() => {
  if (!leadBreakdown.value) {
    return '等待对比结果'
  }

  const { teamA, teamB, aLead, bLead } = leadBreakdown.value

  if (aLead === bLead) {
    return '两队整体势均力敌'
  }
  return aLead > bLead ? `${teamA.team} 综合占优` : `${teamB.team} 综合占优`
})

const deepConclusionSummary = computed(() => {
  if (!leadBreakdown.value) {
    return '请选择两支球队并执行对比。'
  }

  const { teamA, teamB, aLead, bLead } = leadBreakdown.value
  if (aLead === bLead) {
    return `${teamA.team} 与 ${teamB.team} 在${metricText.value}维度整体势均力敌，比赛走向更依赖关键回合执行质量。`
  }
  const winner = aLead > bLead ? teamA.team : teamB.team
  return `${winner} 在${metricText.value}维度建立了更稳定的多指标优势，整体竞争力略高于对手。`
})

const deepConclusionPoints = computed(() => {
  if (!leadBreakdown.value) {
    return []
  }

  const { teamA, teamB, aLead, bLead, ties } = leadBreakdown.value
  const topGapMetric = duelMetrics.value.reduce((prev, curr) => (curr.diffValue > prev.diffValue ? curr : prev))
  const winRateA = Number((teamA.winRate * 100).toFixed(1))
  const winRateB = Number((teamB.winRate * 100).toFixed(1))
  const offenseLeader = teamA.points === teamB.points ? '双方' : teamA.points > teamB.points ? teamA.team : teamB.team
  const reboundLeader = teamA.rebounds === teamB.rebounds ? '双方' : teamA.rebounds > teamB.rebounds ? teamA.team : teamB.team
  const dominantTeam = aLead === bLead ? `${teamA.team} 与 ${teamB.team}` : aLead > bLead ? teamA.team : teamB.team

  const modeAdviceMap = {
    all: `策略建议：优先围绕「${topGapMetric.metric}」设计针对性轮换，${dominantTeam}可继续放大现有强项。`,
    offense: `策略建议：进攻模式下应重点优化节奏与空间，${dominantTeam}可尝试提升外线牵制和转换效率。`,
    defense: `策略建议：防守模式下建议优先强化篮板保护与终结点限制，${dominantTeam}可延续防守压迫。`,
  }

  return [
    `指标领先分布：${teamA.team} 领先 ${aLead} 项，${teamB.team} 领先 ${bLead} 项，持平 ${ties} 项。`,
    `最大差距项：${topGapMetric.metric}。${teamA.team} 为 ${topGapMetric.aDisplay}，${teamB.team} 为 ${topGapMetric.bDisplay}，领先方是 ${topGapMetric.leader}（差值 ${topGapMetric.diffDisplay}）。`,
    `胜率对照：${teamA.team} ${winRateA}% vs ${teamB.team} ${winRateB}%，相差 ${Math.abs(winRateA - winRateB).toFixed(1)} 个百分点。`,
    `风格判断：进攻端由${offenseLeader}占优，篮板控制由${reboundLeader}占优。`,
    modeAdviceMap[selectedMetric.value],
  ]
})

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

const swapTeams = () => {
  if (!selectedTeamA.value && !selectedTeamB.value) return
  const temp = selectedTeamA.value
  selectedTeamA.value = selectedTeamB.value
  selectedTeamB.value = temp
}

const randomCompare = () => {
  if (teamNames.value.length < 2) return
  const firstIdx = Math.floor(Math.random() * teamNames.value.length)
  let secondIdx = Math.floor(Math.random() * teamNames.value.length)
  while (secondIdx === firstIdx) {
    secondIdx = Math.floor(Math.random() * teamNames.value.length)
  }
  selectedTeamA.value = teamNames.value[firstIdx]
  selectedTeamB.value = teamNames.value[secondIdx]
  runComparison()
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
  padding: 0;
  background-color: transparent;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 30px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #909399;
  font-size: 16px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.overview-card {
  background: linear-gradient(135deg, #ffffff 0%, #f4f6fb 100%);
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 14px 16px;
}

.overview-card.east {
  background: linear-gradient(135deg, #fff6f6 0%, #ffe8e8 100%);
}

.overview-card.west {
  background: linear-gradient(135deg, #f3f8ff 0%, #e7f0ff 100%);
}

.overview-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.overview-value {
  font-size: 24px;
  font-weight: 800;
  color: #1f2a44;
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
  font-size: 14px;
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

.quick-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

/* 结果区域 */
.result-row {
  margin-bottom: 24px;
}

.deep-insight-row {
  margin-bottom: 24px;
}

.battle-card,
.duel-card {
  border-radius: 16px;
  border: none;
  height: 100%;
}

.battle-winner {
  font-size: 28px;
  font-weight: 800;
  color: #1f2a44;
  margin-bottom: 10px;
}

.battle-desc {
  color: #606266;
  font-size: 17px;
  line-height: 1.7;
  margin-bottom: 10px;
}

.battle-points {
  margin: 0 0 14px 0;
  padding-left: 18px;
  color: #606266;
  font-size: 15px;
  line-height: 1.7;
}

.battle-points li {
  margin-bottom: 6px;
}

.battle-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.battle-tags :deep(.el-tag) {
  font-size: 13px;
}

.duel-list {
  display: grid;
  gap: 16px;
}

.duel-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafbfc;
  padding: 14px 16px;
}

.duel-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.duel-metric {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.duel-leader {
  font-size: 15px;
  color: #909399;
}

.duel-progress-row {
  display: grid;
  grid-template-columns: 150px 1fr 84px;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.duel-team-label {
  font-size: 15px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.duel-team-label.team-a {
  color: #C9082A;
}

.duel-team-label.team-b {
  color: #006BB6;
}

.duel-progress {
  width: 100%;
}

.duel-value {
  font-size: 18px;
  color: #303133;
  text-align: right;
  font-weight: 600;
}

.duel-diff {
  font-size: 15px;
  color: #909399;
  text-align: right;
}

.duel-card .header-title {
  font-size: 22px;
}

.insight-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.insight-item {
  border: 1px solid #ebeef5;
  background: #fafbfc;
  border-radius: 12px;
  padding: 12px;
}

.insight-metric {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.insight-team {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 2px;
}

.insight-diff {
  font-size: 12px;
  color: #606266;
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
  font-size: 18px;
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

.summary-card :deep(.el-table th) {
  font-size: 18px;
}

.summary-card :deep(.el-table td) {
  font-size: 17px;
}

.summary-card :deep(.el-table .cell) {
  line-height: 1.45;
}

.summary-card :deep(.el-progress__text) {
  font-size: 28px !important;
  font-weight: 700;
}

.win-rate-summary {
  margin-top: 18px;
}

.wr-label {
  font-size: 17px;
  color: #909399;
  margin-bottom: 10px;
}

/* 列表卡片 */
.team-list-card {
  border-radius: 16px;
  border: none;
}

.header-tip {
  font-size: 13px;
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

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .insight-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .duel-progress-row {
    grid-template-columns: 120px 1fr 72px;
  }
}

@media (max-width: 768px) {
  .team-comparison-container {
    padding: 16px;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .config-grid {
    gap: 16px;
  }

  .team-selector {
    width: 100%;
  }

  .filter-group {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }

  .season-select {
    width: 100%;
  }

  .action-box,
  .compare-btn {
    width: 100%;
  }

  .quick-actions {
    justify-content: space-between;
  }

  .insight-grid {
    grid-template-columns: 1fr;
  }

  .duel-progress-row {
    grid-template-columns: 88px 1fr 64px;
    gap: 6px;
  }
}
</style>

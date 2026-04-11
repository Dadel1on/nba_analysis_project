<template>
  <div class="prediction" :class="{ embedded }">
    <h1 v-if="!embedded">表现预测中心</h1>

    <el-row :gutter="24">
      <!-- 左侧：球员搜索与选择 -->
      <el-col :span="24" :lg="10">
        <el-card class="search-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Search /></el-icon> 球员表现预测
              </span>
              <el-tag type="success" effect="dark" round size="small">实时模型</el-tag>
            </div>
          </template>

          <p class="card-desc">输入球员姓名，基于历史数据预测下一场表现。</p>

          <div class="search-box">
            <el-input
              v-model="searchName"
              placeholder="搜索球员 (例如: James, Curry)"
              clearable
              @keyup.enter="fetchPlayers"
              class="custom-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" :loading="loadingPlayers" @click="fetchPlayers" icon="Search">
              搜索
            </el-button>
          </div>

          <div class="table-container">
            <el-table
              v-if="players.length"
              :data="players"
              style="width: 100%"
              max-height="850"
              @row-click="selectPlayer"
              highlight-current-row
              class="custom-table"
            >
              <el-table-column prop="name" label="姓名" min-width="140" show-overflow-tooltip />
              <el-table-column prop="team" label="球队" min-width="100" />
              <el-table-column label="场均 (得/篮/助)" min-width="150">
                <template #default="scope">
                  <span class="stat-tag pts">{{ format2(scope.row.stats.points) }}</span>
                  <span class="stat-tag reb">{{ format2(scope.row.stats.rebounds) }}</span>
                  <span class="stat-tag ast">{{ format2(scope.row.stats.assists) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="96" fixed="right">
                <template #default="scope">
                  <el-button
                    v-if="selectedPlayer?.id !== scope.row.id"
                    size="small"
                    type="primary"
                    plain
                    :icon="Check"
                    @click.stop="selectPlayer(scope.row)"
                  >
                    选择
                  </el-button>
                  <el-button v-else size="small" type="success" :icon="Check" disabled>
                    已选
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="loadingPlayers ? '数据搜索中...' : '请输入关键词开始搜索'" :image-size="100" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：预测结果与分析 -->
      <el-col :span="24" :lg="14">
        <el-card v-if="selectedPlayer" class="result-card" shadow="always">
          <template #header>
            <div class="result-header">
              <div class="player-info-large">
                <div class="player-avatar-mini">
                  {{ selectedPlayer.name.charAt(0) }}
                </div>
                <div class="player-text">
                  <div class="p-name">{{ selectedPlayer.name }}</div>
                  <div class="p-sub">{{ selectedPlayer.team }} · {{ selectedPlayer.position || '球员' }}</div>
                </div>
              </div>
              <el-button 
                type="primary" 
                size="large"
                :loading="loadingPrediction" 
                @click="runPrediction"
                icon="MagicStick"
                class="predict-btn"
              >
                生成即时预测
              </el-button>
            </div>
          </template>

          <div class="prediction-content">
            <!-- 基础数据对比 -->
            <div class="section-title">基础数据概览</div>
            <el-row :gutter="20" class="stats-grid">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="label">场均得分</div>
                    <div class="value">{{ format2(selectedPlayer.stats.points) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="label">场均篮板</div>
                    <div class="value">{{ format2(selectedPlayer.stats.rebounds) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="label">场均助攻</div>
                    <div class="value">{{ format2(selectedPlayer.stats.assists) }}</div>
                </div>
              </el-col>
            </el-row>

            <el-divider v-if="prediction || predictionError" />

            <!-- 预测结果展示 -->
            <transition name="el-fade-in-linear">
              <div v-if="prediction" class="prediction-highlight">
                <div class="highlight-title">下一场表现预测</div>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <div class="predict-stat-box pts">
                      <div class="p-label">预测得分</div>
                      <div class="p-value">{{ prediction.predicted_stats.points?.toFixed(2) }}</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="predict-stat-box reb">
                      <div class="p-label">预测篮板</div>
                      <div class="p-value">{{ prediction.predicted_stats.rebounds?.toFixed(2) }}</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="predict-stat-box ast">
                      <div class="p-label">预测助攻</div>
                      <div class="p-value">{{ prediction.predicted_stats.assists?.toFixed(2) }}</div>
                    </div>
                  </el-col>
                </el-row>
                <div class="confidence-bar">
                  <div class="c-label">
                    <span>模型置信度</span>
                    <el-tooltip
                      placement="top"
                      effect="dark"
                      content="0~1 的可信度分数，用于表示系统对本次预测的把握程度（越高越可靠）。它不是“预测一定正确”的概率。"
                    >
                      <el-icon class="help-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                  </div>
                  <el-progress 
                    :percentage="Math.round(prediction.confidence * 100)" 
                    :color="prediction.confidence > 0.8 ? '#67C23A' : '#E6A23C'"
                    :stroke-width="10"
                  />
                </div>
              </div>
            </transition>

            <el-alert
              v-if="predictionError"
              type="error"
              :title="predictionError"
              show-icon
              closable
            />

            <!-- 深度分析标签页 -->
            <el-tabs class="analysis-tabs" style="margin-top: 24px">
              <el-tab-pane label="赛季趋势预测">
                <div class="tab-header">
                  <span class="tab-tip">基于时间序列模型的长期走势分析</span>
                  <el-button 
                    size="small" 
                    type="primary" 
                    plain
                    :loading="loadingSeasonTrend" 
                    @click="runSeasonTrendPrediction"
                  >
                    刷新趋势图表
                  </el-button>
                </div>
                <el-alert v-if="seasonTrendError" type="error" :title="seasonTrendError" show-icon />
                <div v-if="seasonTrend" ref="trendChartRef" class="chart-box"></div>
                <el-empty v-else description="点击上方按钮生成趋势分析" :image-size="60" />
              </el-tab-pane>

              <el-tab-pane label="模型可解释性 (SHAP)">
                <div class="tab-header">
                  <span class="tab-tip">解析影响预测结果的关键特征因子</span>
                  <el-button 
                    size="small" 
                    type="primary" 
                    plain
                    :loading="loadingExplainability" 
                    @click="loadExplainability"
                  >
                    加载特征分析
                  </el-button>
                </div>
                <el-alert v-if="explainError" type="error" :title="explainError" show-icon />
                <div v-if="explainability" class="explain-content">
                  <div class="explain-info">
                    <el-tag size="small" :type="isFactExplainability ? 'success' : 'info'">
                      {{ isFactExplainability ? '事实表驱动' : '基础模型' }}
                    </el-tag>
                    <span class="model-ver">版本: {{ explainability.model_version }}</span>
                  </div>
                  <div ref="explainChartRef" class="chart-box explain"></div>
                  <div class="factor-legend">
                    <span class="legend-item"><i class="dot pos"></i> 正向贡献</span>
                    <span class="legend-item"><i class="dot neg"></i> 负向影响</span>
                  </div>
                </div>
                <el-empty v-else description="点击上方按钮加载特征因子分析" :image-size="60" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>

        <el-card v-else class="empty-result-card" shadow="never">
          <el-empty description="在左侧选择一名球员以开启深度预测分析" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { Check, InfoFilled, Search, User, MagicStick } from '@element-plus/icons-vue'
import {
  getPredictionExplainability,
  predictPlayerPerformance,
  predictSeasonTrend,
} from '@/api/prediction'
import { searchPlayers } from '@/api/players'
import { getTeams } from '@/api/teams'
import type {
  PlayerPredictionResult,
  PlayerSummary,
  PredictionExplainability,
  SeasonTrendPredictionResult,
} from '@/api/types'

const props = defineProps<{ embedded?: boolean }>()

const searchName = ref('')
const players = ref<PlayerSummary[]>([])
const selectedPlayer = ref<PlayerSummary | null>(null)

const loadingPlayers = ref(false)
const loadingPrediction = ref(false)
const prediction = ref<PlayerPredictionResult | null>(null)
const predictionError = ref('')

const loadingSeasonTrend = ref(false)
const seasonTrend = ref<SeasonTrendPredictionResult | null>(null)
const seasonTrendError = ref('')
const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const loadingExplainability = ref(false)
const explainability = ref<PredictionExplainability | null>(null)
const explainError = ref('')
const explainChartRef = ref<HTMLElement | null>(null)
let explainChart: echarts.ECharts | null = null

const isFactExplainability = computed(() => explainability.value?.model_version?.toLowerCase().startsWith('fact') ?? false)
const sortedExplainFactors = computed(() => {
  const factors = explainability.value?.key_factors || []
  return [...factors].sort((a, b) => Math.abs(b.contribution) - Math.abs(a.contribution))
})

const format2 = (value: unknown) => {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '-'
}

const fetchPlayers = async () => {
  loadingPlayers.value = true
  try {
    const data = await searchPlayers({
      name: searchName.value,
      page: 1,
      limit: 50,
    })
    // 过滤掉位置未知的球员
    players.value = data.filter(p => {
      const pos = p.position?.toLowerCase() || ''
      return pos && 
             !pos.includes('未知') && 
             !pos.includes('unknown') && 
             !pos.includes('unk')
    })
  } catch {
    players.value = []
  } finally {
    loadingPlayers.value = false
  }
}

const selectPlayer = async (p: PlayerSummary) => {
  selectedPlayer.value = p
  prediction.value = null
  seasonTrend.value = null
  explainability.value = null
  trendChart?.dispose()
  trendChart = null
  explainChart?.dispose()
  explainChart = null
  predictionError.value = ''
  seasonTrendError.value = ''
  explainError.value = ''

  if (props.embedded) {
    await runPrediction()
  }
}

const runPrediction = async () => {
  if (!selectedPlayer.value) return
  loadingPrediction.value = true
  prediction.value = null
  predictionError.value = ''

  try {
    prediction.value = await predictPlayerPerformance(selectedPlayer.value.id)
    await loadExplainability()
  } catch (e) {
    const details = e instanceof Error ? `（${e.message}）` : ''
    predictionError.value = `预测失败：请检查后端是否运行、数据库是否可连接${details}`
  } finally {
    loadingPrediction.value = false
  }
}

const runSeasonTrendPrediction = async () => {
  if (!selectedPlayer.value) return
  loadingSeasonTrend.value = true
  seasonTrend.value = null
  seasonTrendError.value = ''

  try {
    seasonTrend.value = await predictSeasonTrend(selectedPlayer.value.id)
    await nextTick()
    renderTrendChart()
  } catch (e) {
    const details = e instanceof Error ? `（${e.message}）` : ''
    seasonTrendError.value = `趋势预测失败${details}`
  } finally {
    loadingSeasonTrend.value = false
  }
}

const loadExplainability = async () => {
  if (!selectedPlayer.value) return
  loadingExplainability.value = true
  explainability.value = null
  explainError.value = ''

  try {
    explainability.value = await getPredictionExplainability(selectedPlayer.value.id)
  } catch (e) {
    const details = e instanceof Error ? `（${e.message}）` : ''
    explainError.value = `可解释性加载失败${details}`
  } finally {
    loadingExplainability.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value || !seasonTrend.value) return

  const history = seasonTrend.value.history || []
  const forecast = seasonTrend.value.forecast || []
  if (!history.length && !forecast.length) return

  const categories = [...history.map((p) => p.season), ...forecast.map((p) => p.season)]
  if (!categories.length) return

  const lastHistory = history.length ? history[history.length - 1] : null

  const series = [
    { key: 'points' as const, name: '得分', color: '#ef4444' },
    { key: 'rebounds' as const, name: '篮板', color: '#3b82f6' },
    { key: 'assists' as const, name: '助攻', color: '#22c55e' },
  ].flatMap((metric) => {
    const historyData = history.map((p) => Number(p[metric.key]))
    const forecastData = Array(history.length).fill(null) as (number | null)[]
    if (lastHistory) {
      forecastData.push(Number(lastHistory[metric.key]))
      forecastData.push(...forecast.map((p) => Number(p[metric.key])))
    } else {
      forecastData.push(...forecast.map((p) => Number(p[metric.key])))
    }

    return [
      {
        name: `${metric.name}（历史）`,
        type: 'line',
        data: historyData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: metric.color },
        itemStyle: { color: metric.color },
        emphasis: { focus: 'series' },
      },
      {
        name: `${metric.name}（预测）`,
        type: 'line',
        data: forecastData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, type: 'dashed', color: metric.color },
        itemStyle: { color: metric.color },
        connectNulls: false,
        emphasis: { focus: 'series' },
      },
    ]
  })

  trendChart?.dispose()
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis',
      formatter: (params: unknown) => {
        const items = Array.isArray(params) ? params : [params]
        const title = (items[0] as any)?.axisValueLabel ?? ''
        const filtered = items.filter((it: any) => {
          const raw = it?.data && typeof it.data === 'object' && 'value' in it.data ? it.data.value : it?.data
          const num = Number(raw)
          return raw != null && Number.isFinite(num)
        })
        const lines = filtered
          .map((it: any) => {
            const raw = it?.data && typeof it.data === 'object' && 'value' in it.data ? it.data.value : it?.data
            return `<div><span style="display:inline-block;margin-right:6px;border-radius:10px;width:10px;height:10px;background-color:${it.color};"></span>${it.seriesName}<span style="float:right;font-weight:900">${format2(raw)}</span></div>`
          })
          .join('')
        return lines ? `<div style="font-weight:700;margin-bottom:6px">${title}</div>${lines}` : title
      },
    },
    legend: { top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 48, containLabel: true },
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value' },
    series,
  })
}

const renderExplainChart = () => {
  if (!explainChartRef.value) return
  const factors = sortedExplainFactors.value || []
  if (!factors.length) return

  const names = factors.map((f) => f.factor)
  const values = factors.map((f) => Number(f.contribution))
  const colors = values.map((v) => (v >= 0 ? '#22c55e' : '#ef4444'))

  explainChart?.dispose()
  explainChart = echarts.init(explainChartRef.value)
  explainChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      valueFormatter: (v: unknown) => format2(v),
    },
    grid: { left: 140, right: 20, top: 20, bottom: 20, containLabel: false },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, inverse: true },
    series: [
      {
        type: 'bar',
        data: values.map((v, i) => ({ value: v, itemStyle: { color: colors[i] } })),
        barWidth: 14,
      },
    ],
  })
}

watch(sortedExplainFactors, async (factors) => {
  if (!factors.length) {
    explainChart?.dispose()
    explainChart = null
    return
  }
  await nextTick()
  renderExplainChart()
})

onMounted(() => {
  fetchPlayers()
})

onUnmounted(() => {
  trendChart?.dispose()
  explainChart?.dispose()
})
</script>

<style scoped>
.prediction {
  padding: 0;
  background-color: transparent;
  min-height: calc(100vh - 100px);
  margin-top: 10px;
}

h1 {
  margin-bottom: 30px;
  font-weight: 800;
  color: #1f2a44;
  font-size: 32px;
  letter-spacing: -0.5px;
}

/* 搜索卡片样式 */
.search-card {
  border-radius: 16px;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 19px;
  font-weight: 800;
  color: #1f2a44;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.card-desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 16px;
  font-size: 15px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

.custom-input :deep(.el-input__wrapper):hover {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

.table-container {
  border-radius: 8px;
  overflow: hidden;
}

.search-box .el-button {
  border-radius: 12px;
  padding: 0 28px;
  font-weight: 600;
  font-size: 14px;
}

.custom-table {
  --el-table-header-bg-color: #f5f7fa;
  --el-table-row-hover-bg-color: #ecf5ff;
}

.stat-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 4px;
  font-weight: 500;
}

.stat-tag.pts { background: #fee2e2; color: #ef4444; }
.stat-tag.reb { background: #dbeafe; color: #3b82f6; }
.stat-tag.ast { background: #dcfce7; color: #22c55e; }

.is-selected {
  font-weight: bold;
  text-decoration: underline;
}

/* 结果卡片样式 */
.result-card, .empty-result-card {
  border-radius: 16px;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  background: #ffffff;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.player-info-large {
  display: flex;
  align-items: center;
  gap: 16px;
}

.player-avatar-mini {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #006BB6 0%, #004b7f 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0, 107, 182, 0.2);
}

.p-name {
  font-size: 26px;
  font-weight: 800;
  color: #1f2a44;
  line-height: 1.2;
}

.p-sub {
  font-size: 15px;
  color: #909399;
  font-weight: 500;
  margin-top: 4px;
}

.predict-btn {
  border-radius: 12px;
  padding: 14px 32px;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #409eff 0%, #3054ff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
  color: white;
}

.predict-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 20px 0 16px;
  color: #303133;
  padding-left: 10px;
  border-left: 4px solid #006BB6;
}

.stats-grid .stat-item {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 10px;
  text-align: center;
  border: 1px solid #edf2f7;
}

.stat-item .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-item .value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

/* 预测高亮区 */
.prediction-highlight {
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #d6eaff;
  margin: 20px 0;
}

.highlight-title {
  font-size: 18px;
  font-weight: 700;
  color: #006BB6;
  margin-bottom: 20px;
  text-align: center;
}

.predict-stat-box {
  text-align: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 107, 182, 0.08);
  transition: transform 0.3s;
}

.predict-stat-box:hover {
  transform: translateY(-4px);
}

.predict-stat-box.pts .p-value { color: #ef4444; }
.predict-stat-box.reb .p-value { color: #3b82f6; }
.predict-stat-box.ast .p-value { color: #22c55e; }

.p-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.p-value {
  font-size: 28px;
  font-weight: 800;
}

.confidence-bar {
  margin-top: 24px;
  padding: 0 10px;
}

.c-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.help-icon {
  font-size: 14px;
  color: #a8abb2;
  cursor: help;
}

/* 标签页样式 */
.analysis-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  height: 50px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tab-tip {
  font-size: 13px;
  color: #909399;
  font-style: italic;
}

.chart-box {
  height: 350px;
  width: 100%;
  margin-top: 10px;
}

.chart-box.explain {
  height: 300px;
}

.explain-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.model-ver {
  font-size: 12px;
  color: #909399;
}

.factor-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.pos { background: #67C23A; }
.dot.neg { background: #F56C6C; }

.empty-result-card {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  border: 2px dashed #e4e7ed;
}
</style>

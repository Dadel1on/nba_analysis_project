<template>
  <div class="spark-analysis">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Spark 数据分析</h1>
        <p class="page-subtitle">利用大数据处理引擎进行球员表现预测与指标分析</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          size="large"
          :loading="sparkRunning" 
          :icon="VideoPlay" 
          @click="handleRunSpark"
          class="start-btn"
        >
          开始全量分析
        </el-button>
      </div>
    </div>

    <!-- Model Introduction Section (New) -->
    <el-card shadow="hover" class="main-card mb-4 model-info-card">
      <div class="model-info-content">
        <div class="model-header-main">
          <div class="model-badge">
            <el-icon><Cpu /></el-icon>
            <span>当前模型：Random Forest Regressor (RF-v1.2)</span>
          </div>
          <p class="model-main-desc">
            本系统采用基于 <strong>Spark MLlib</strong> 构建的随机森林回归模型，通过对球员历史赛季的多维指标（得分、效率、出场时间、球队权重等）进行深度特征工程，生成对球员未来表现的量化预测。
          </p>
        </div>
        <el-divider direction="vertical" border-style="dashed" class="model-divider" />
        <div class="logic-grid">
          <div class="logic-item">
            <div class="logic-label">
              <el-icon><DataAnalysis /></el-icon> 预测逻辑
            </div>
            <div class="logic-value">非线性特征提取 + 决策树集成学习</div>
          </div>
          <div class="logic-item">
            <div class="logic-label">
              <el-icon><Histogram /></el-icon> 置信度算法
            </div>
            <div class="logic-value">基于样本方差与模型收敛度的加权评分</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Analysis Results Section -->
    <el-card v-if="sparkHistory.some(h => h.status === 'success')" shadow="hover" class="main-card mb-4">
      <template #header>
        <div class="header-row">
          <div class="title-with-tag">
            <span class="card-title">分析结果明细</span>
            <el-tag size="small" type="success" effect="plain" class="ml-2">最新预测</el-tag>
          </div>
          <div class="actions">
            <el-button :icon="DataLine" size="small" @click="loadPredictions">刷新结果</el-button>
          </div>
        </div>
      </template>

      <!-- Data Visualizations (New Section) -->
      <div class="charts-row">
        <div class="chart-container-box full-width">
          <div class="chart-header">
            <el-icon class="mr-1"><TrendCharts /></el-icon>
            <span>Top 15 球员多维预测数据对比</span>
          </div>
          <div ref="topPlayersChartRef" class="echart-container"></div>
        </div>
      </div>

      <!-- Additional Charts Row (New) -->
      <el-row :gutter="20" class="charts-row mt-4">
        <el-col :span="12">
          <div class="chart-container-box">
            <div class="chart-header">
              <el-icon class="mr-1"><User /></el-icon>
              <span>球员多维能力剖析: {{ selectedPlayer?.playerName || '-' }}</span>
            </div>
            <div ref="radarChartRef" class="echart-container"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-container-box">
            <div class="chart-header">
              <el-icon class="mr-1"><Odometer /></el-icon>
              <span>本批次预测置信度概览</span>
            </div>
            <div ref="gaugeChartRef" class="echart-container"></div>
          </div>
        </el-col>
      </el-row>

      <div class="results-desc">
        <p>以下是 Spark 任务生成的球员表现预测数据。模型版本：{{ predictionResults[0]?.modelVersion }}，生成时间：{{ predictionResults[0]?.createdAt }}</p>
      </div>

      <el-table 
        :data="predictionResults" 
        v-loading="resultsLoading" 
        stripe
        highlight-current-row
        @row-click="handleRowClick"
        style="width: 100%; font-size: 16px;"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', fontSize: '16px' }"
      >
        <el-table-column prop="playerName" label="球员姓名" min-width="120">
          <template #default="scope">
            <span class="player-name">{{ scope.row.playerName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="predictedPoints" label="预测得分" width="100" sortable>
          <template #default="scope">
            <span class="stat-highlight">{{ scope.row.predictedPoints.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="predictedRebounds" label="预测篮板" width="100" sortable>
          <template #default="scope">
            <span>{{ scope.row.predictedRebounds.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="predictedAssists" label="预测助攻" width="100" sortable>
          <template #default="scope">
            <span>{{ scope.row.predictedAssists.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="120" sortable>
          <template #default="scope">
            <el-progress 
              :percentage="Math.round(scope.row.confidence * 100)" 
              :status="scope.row.confidence > 0.8 ? 'success' : (scope.row.confidence > 0.5 ? 'warning' : 'exception')"
              :stroke-width="12"
            />
          </template>
        </el-table-column>
        <el-table-column prop="modelVersion" label="模型版本" width="120" />
        <template #empty>
          <el-empty description="暂无预测数据，请尝试重新分析" :image-size="80" />
        </template>
      </el-table>
    </el-card>

    <!-- Spark Execution Summary & History (Merged & Beautified) -->
    <el-card shadow="hover" class="main-card history-card">
      <template #header>
        <div class="header-row">
          <div class="title-with-tag">
            <span class="card-title">任务分析概况</span>
            <el-tag size="small" type="info" effect="plain" class="ml-2">分析引擎动态</el-tag>
          </div>
          <div class="actions">
            <el-button :icon="Refresh" size="small" @click="loadSparkHistory(false)">刷新记录</el-button>
          </div>
        </div>
      </template>

      <!-- Integrated Summary Stats Row -->
      <div class="compact-stats-bar">
        <div class="compact-stat-item">
          <span class="compact-stat-label">总次数</span>
          <span class="compact-stat-value">{{ stats.total }}</span>
        </div>
        <div class="compact-stat-divider"></div>
        <div class="compact-stat-item success">
          <span class="compact-stat-label">成功</span>
          <span class="compact-stat-value">{{ stats.success }}</span>
        </div>
        <div class="compact-stat-divider"></div>
        <div class="compact-stat-item failed">
          <span class="compact-stat-label">失败</span>
          <span class="compact-stat-value">{{ stats.failed }}</span>
        </div>
        <div class="compact-stat-divider"></div>
        <div class="compact-stat-item running">
          <span class="compact-stat-label">进行中</span>
          <span class="compact-stat-value">
            {{ stats.running }}
            <el-icon v-if="stats.running > 0" class="is-loading ml-1"><Loading /></el-icon>
          </span>
        </div>
      </div>

      <div class="latest-status-section" v-if="sparkHistory.length > 0">
        <div class="status-header">
          <span class="section-subtitle">最新执行动态</span>
          <el-button type="primary" link @click="showDetail(sparkHistory[0].detail)">
            查看执行日志
          </el-button>
        </div>
        
        <div class="status-banner" :class="sparkHistory[0].status">
          <div class="banner-left">
            <el-icon class="status-icon">
              <CircleCheck v-if="sparkHistory[0].status === 'success'" />
              <CircleClose v-else-if="sparkHistory[0].status === 'failed'" />
              <Loading v-else-if="sparkHistory[0].status === 'running'" class="is-loading" />
              <InfoFilled v-else />
            </el-icon>
            <div class="job-info">
              <div class="job-name-text">{{ sparkHistory[0].jobName }}</div>
              <div class="job-time-text">{{ sparkHistory[0].createdAt }}</div>
            </div>
          </div>
          <div class="banner-right">
            <el-tag 
              :type="sparkStatusTag(sparkHistory[0].status)" 
              effect="dark"
              class="status-tag-large"
            >
              {{ sparkHistory[0].status.toUpperCase() }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无执行历史记录" :image-size="80" />
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务执行详情"
      width="60%"
      destroy-on-close
    >
      <div class="log-container">
        <pre>{{ selectedDetail || '暂无详细日志信息' }}</pre>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { VideoPlay, Refresh, InfoFilled, CircleCheck, CircleClose, Loading, DataLine, PieChart, TrendCharts, Cpu, DataAnalysis, Histogram, User, Odometer } from '@element-plus/icons-vue'
import { getSparkHistory, runSparkJob, getSparkPredictions } from '@/api/admin'
import type { SparkJobRunItem, PredictionResultItem } from '@/api/types'

const sparkLoading = ref(false)
const resultsLoading = ref(false)
const sparkRunning = ref(false)
const sparkHistory = ref<SparkJobRunItem[]>([])
const predictionResults = ref<PredictionResultItem[]>([])
const selectedPlayer = ref<PredictionResultItem | null>(null)

const detailDialogVisible = ref(false)
const selectedDetail = ref('')

const topPlayersChartRef = ref<HTMLElement | null>(null)
let topPlayersChart: echarts.ECharts | null = null

const radarChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null

const gaugeChartRef = ref<HTMLElement | null>(null)
let gaugeChart: echarts.ECharts | null = null

const stats = computed(() => {
  const total = sparkHistory.value.length
  const success = sparkHistory.value.filter(h => h.status === 'success').length
  const failed = sparkHistory.value.filter(h => h.status === 'failed').length
  const running = sparkHistory.value.filter(h => h.status === 'running').length
  return { total, success, failed, running }
})

const sparkStatusTag = (status: SparkJobRunItem['status']) => {
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  if (status === 'running') return 'primary'
  return 'info'
}

const showDetail = (detail: string) => {
  selectedDetail.value = detail
  detailDialogVisible.value = true
}

const loadSparkHistory = async (silent = false) => {
  sparkLoading.value = true
  try {
    sparkHistory.value = await getSparkHistory()
    // If we have successful runs, load predictions too
    if (sparkHistory.value.some(h => h.status === 'success')) {
      loadPredictions()
    }
    if (!silent) {
      ElMessage.success('历史记录已更新')
    }
  } catch (err) {
    if (!silent) {
      ElMessage.error('获取历史记录失败')
    }
  } finally {
    sparkLoading.value = false
  }
}

const loadPredictions = async () => {
  resultsLoading.value = true
  try {
    predictionResults.value = await getSparkPredictions()
    if (predictionResults.value.length > 0 && !selectedPlayer.value) {
      selectedPlayer.value = predictionResults.value[0]
    }
    nextTick(() => {
      initCharts()
    })
  } catch (err) {
    console.error('Failed to load predictions:', err)
  } finally {
    resultsLoading.value = false
  }
}

const handleRowClick = (row: PredictionResultItem) => {
  selectedPlayer.value = row
  updateRadarChart()
}

const initCharts = () => {
  try {
    if (topPlayersChartRef.value) {
      if (topPlayersChart) topPlayersChart.dispose()
      topPlayersChart = echarts.init(topPlayersChartRef.value)
      updateTopPlayersChart()
    }
    if (radarChartRef.value) {
      if (radarChart) radarChart.dispose()
      radarChart = echarts.init(radarChartRef.value)
      updateRadarChart()
    }
    if (gaugeChartRef.value) {
      if (gaugeChart) gaugeChart.dispose()
      gaugeChart = echarts.init(gaugeChartRef.value)
      updateGaugeChart()
    }
  } catch (err) {
    console.error('Charts initialization failed:', err)
  }
}

const updateTopPlayersChart = () => {
  if (!topPlayersChart || predictionResults.value.length === 0) return
  
  const displayCount = Math.min(predictionResults.value.length, 15)
  const topPlayers = [...predictionResults.value]
    .sort((a, b) => b.predictedPoints - a.predictedPoints)
    .slice(0, displayCount)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['预测得分', '预测篮板', '预测助攻'],
      bottom: '2%',
      textStyle: { fontSize: 14 }
    },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: topPlayers.map(p => p.playerName),
      axisLabel: { 
        interval: 0, 
        rotate: 25,
        fontSize: 14,
        fontWeight: 'bold',
        color: '#606266'
      }
    },
    yAxis: { type: 'value', name: '数值' },
    series: [
      {
        name: '预测得分',
        type: 'bar',
        barGap: '20%',
        barMaxWidth: 30,
        emphasis: { focus: 'series' },
        data: topPlayers.map(p => p.predictedPoints),
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#73b9ff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '预测篮板',
        type: 'bar',
        barMaxWidth: 30,
        emphasis: { focus: 'series' },
        data: topPlayers.map(p => p.predictedRebounds),
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#95d475' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '预测助攻',
        type: 'bar',
        barMaxWidth: 30,
        emphasis: { focus: 'series' },
        data: topPlayers.map(p => p.predictedAssists),
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e6a23c' },
            { offset: 1, color: '#f3d19e' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  topPlayersChart.setOption(option)
}

const updateRadarChart = () => {
  if (!radarChart || !selectedPlayer.value) return

  const p = selectedPlayer.value

  const option: echarts.EChartsOption = {
    radar: {
      indicator: [
        { name: '预测得分', max: 35 },
        { name: '预测篮板', max: 15 },
        { name: '预测助攻', max: 12 },
        { name: '置信度', max: 1 },
        { name: '综合效率', max: 100 }
      ],
      radius: '65%',
      axisName: {
        fontSize: 14,
        fontWeight: 'bold',
        color: '#606266'
      }
    },
    series: [
      {
        name: p.playerName,
        type: 'radar',
        data: [
          {
            value: [
              p.predictedPoints,
              p.predictedRebounds,
              p.predictedAssists,
              p.confidence,
              p.confidence * 100 // mock efficiency
            ],
            name: p.playerName,
            areaStyle: { color: 'rgba(64, 158, 255, 0.4)' },
            lineStyle: { color: '#409eff' },
            itemStyle: { color: '#409eff' }
          }
        ]
      }
    ]
  }
  radarChart.setOption(option)
}

const updateGaugeChart = () => {
  if (!gaugeChart || predictionResults.value.length === 0) return

  const avgConfidence = predictionResults.value.reduce((acc, p) => acc + p.confidence, 0) / predictionResults.value.length

  const option: echarts.EChartsOption = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 1,
        splitNumber: 5,
        radius: '90%',
        center: ['50%', '70%'],
        axisLine: {
          lineStyle: {
            width: 6,
            color: [
              [0.7, '#f56c6c'],
              [0.9, '#e6a23c'],
              [1, '#67c23a']
            ]
          }
        },
        pointer: { icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z', length: '12%', width: 10, offsetCenter: [0, '-60%'], itemStyle: { color: 'auto' } },
        axisTick: { length: 12, lineStyle: { color: 'auto', width: 2 } },
        splitLine: { length: 20, lineStyle: { color: 'auto', width: 5 } },
        axisLabel: { color: '#464646', fontSize: 16, distance: -45, formatter: (value: number) => (value * 100).toFixed(0) + '%' },
        title: { offsetCenter: [0, '-20%'], fontSize: 18, fontWeight: 'bold' },
        detail: {
          fontSize: 32,
          fontWeight: 'bold',
          offsetCenter: [0, '0%'],
          valueAnimation: true,
          formatter: (value: number) => (value * 100).toFixed(1) + '%',
          color: 'auto'
        },
        data: [{ value: avgConfidence, name: '平均置信度' }]
      }
    ]
  }
  gaugeChart.setOption(option)
}

const resizeCharts = () => {
  topPlayersChart?.resize()
  radarChart?.resize()
  gaugeChart?.resize()
}

watch(predictionResults, () => {
  nextTick(() => {
    updateTopPlayersChart()
    updateRadarChart()
    updateGaugeChart()
  })
})

const handleRunSpark = async () => {
  sparkRunning.value = true
  try {
    await runSparkJob()
    ElMessage.success('Spark 任务已提交，请稍后刷新查看状态')
    setTimeout(() => loadSparkHistory(true), 1000)
  } catch (err) {
    ElMessage.error('Spark 任务提交失败')
  } finally {
    sparkRunning.value = false
  }
}

onMounted(() => {
  loadSparkHistory(true)
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  topPlayersChart?.dispose()
  radarChart?.dispose()
  gaugeChart?.dispose()
})
</script>

<style scoped>
.spark-analysis {
  padding: 0;
  background-color: transparent;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.start-btn {
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.page-title {
  font-size: 34px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  display: flex;
  align-items: center;
  padding: 20px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-icon {
  font-size: 40px;
  opacity: 0.2;
}

/* Stat Card Colors */
.total { background: linear-gradient(135deg, #ffffff 0%, #f0f2f5 100%); border-left: 4px solid #909399; }
.success { background: linear-gradient(135deg, #ffffff 0%, #f0f9eb 100%); border-left: 4px solid #67c23a; }
.failed { background: linear-gradient(135deg, #ffffff 0%, #fef0f0 100%); border-left: 4px solid #f56c6c; }
.running { background: linear-gradient(135deg, #ffffff 0%, #ecf5ff 100%); border-left: 4px solid #409eff; }

.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.title-with-tag {
  display: flex;
  align-items: center;
}

.ml-2 {
  margin-left: 8px;
}

.mr-1 {
  margin-right: 4px;
}

.spark-desc {
  display: flex;
  align-items: flex-start;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  background-color: #fffaf3;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ffebcc;
  margin-bottom: 20px;
}

.desc-icon {
  font-size: 18px;
  color: #e6a23c;
  margin-right: 12px;
  margin-top: 2px;
}

.spark-desc p {
  margin: 0;
}

.table-container {
  margin-top: 10px;
}

.job-name {
  font-weight: 500;
  color: #409eff;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 10px;
  height: 28px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.detail-text {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
}

.detail-text:hover {
  color: #409eff;
}
.mt-4 {
  margin-top: 24px;
}

.results-desc {
  margin-bottom: 20px;
  color: #606266;
  font-size: 16px;
}

.player-name {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.stat-highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.charts-row {
  margin-bottom: 24px;
}

.chart-container-box {
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  height: 380px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  font-size: 18px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.echart-container {
  flex: 1;
  width: 100%;
  min-height: 300px;
}

.history-card {
  border-top: 4px solid #409eff;
}

.compact-stats-bar {
  display: flex;
  align-items: center;
  background-color: #f8f9fb;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.compact-stat-item {
  display: flex;
  flex-direction: column;
  flex: 1;
  text-align: center;
}

.compact-stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
}

.compact-stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.compact-stat-item.success .compact-stat-value { color: #67c23a; }
.compact-stat-item.failed .compact-stat-value { color: #f56c6c; }
.compact-stat-item.running .compact-stat-value { color: #409eff; }

.compact-stat-divider {
  width: 1px;
  height: 30px;
  background-color: #e4e7ed;
  margin: 0 20px;
}

.latest-status-section {
  padding: 0 4px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.status-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-radius: 8px;
  background-color: #f0f2f5;
  border-left: 6px solid #909399;
}

.status-banner.success {
  background-color: #f0f9eb;
  border-left-color: #67c23a;
}

.status-banner.failed {
  background-color: #fef0f0;
  border-left-color: #f56c6c;
}

.status-banner.running {
  background-color: #ecf5ff;
  border-left-color: #409eff;
}

.banner-left {
  display: flex;
  align-items: center;
}

.status-icon {
  font-size: 28px;
  margin-right: 16px;
  color: inherit;
}

.success .status-icon { color: #67c23a; }
.failed .status-icon { color: #f56c6c; }
.running .status-icon { color: #409eff; }

.job-name-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.job-time-text {
  font-size: 14px;
  color: #909399;
}

.status-tag-large {
  padding: 0 20px;
  height: 36px;
  line-height: 34px;
  font-size: 16px;
  font-weight: bold;
}

.mb-4 {
  margin-bottom: 24px;
}

/* Model Info Card Styles */
.model-info-card {
  background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
  border-left: 5px solid #409eff;
}

.model-info-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.model-header-main {
  flex: 2;
  padding-right: 24px;
}

.model-badge {
  display: inline-flex;
  align-items: center;
  background-color: #409eff;
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 14px;
}

.model-badge .el-icon {
  margin-right: 6px;
  font-size: 16px;
}

.model-main-desc {
  margin: 0;
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
}

.model-divider {
  height: 90px;
  margin: 0 40px;
}

.logic-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.logic-item {
  display: flex;
  flex-direction: column;
}

.logic-label {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.logic-label .el-icon {
  margin-right: 6px;
  color: #409eff;
  font-size: 16px;
}

.logic-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.log-container {
  background-color: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', Courier, monospace;
}

.log-container pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>

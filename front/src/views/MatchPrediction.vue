
<template>
  <div class="prediction-container">
    <div class="page-header">
      <h1>比赛结果预测</h1>
      <p class="subtitle">从对阵设定到情景推演，快速获得更完整的比赛胜率洞察</p>
      <div class="header-tags">
        <el-tag type="info" effect="plain" round>模型: 最近20场状态</el-tag>
        <el-tag type="success" effect="plain" round>主客场修正</el-tag>
        <el-tag type="warning" effect="plain" round>可视化解读</el-tag>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：选择与配置 -->
      <el-col :span="24" :lg="8">
        <el-card class="selection-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Connection /></el-icon> 对阵设置
              </span>
              <el-tag type="info" size="small" round>智能匹配</el-tag>
            </div>
          </template>

          <div class="team-selection-box">
            <div class="selection-item">
              <label>主队 (主场)</label>
              <el-select
                v-model="homeTeam"
                placeholder="选择主队"
                filterable
                clearable
                class="custom-select"
                :loading="loadingTeams"
              >
                <template #prefix>
                  <el-icon><HomeFilled /></el-icon>
                </template>
                <el-option
                  v-for="team in teamOptions"
                  :key="`home-${team}`"
                  :label="team"
                  :value="team"
                />
              </el-select>
            </div>

            <div class="vs-divider">
              <span class="vs-text">VS</span>
            </div>

            <div class="selection-item">
              <label>客队 (客场)</label>
              <el-select
                v-model="awayTeam"
                placeholder="选择客队"
                filterable
                clearable
                class="custom-select"
                :loading="loadingTeams"
              >
                <template #prefix>
                  <el-icon><LocationFilled /></el-icon>
                </template>
                <el-option
                  v-for="team in teamOptions"
                  :key="`away-${team}`"
                  :label="team"
                  :value="team"
                  :disabled="team === homeTeam"
                />
              </el-select>
            </div>

            <div class="inline-actions">
              <el-button :icon="Refresh" plain @click="swapTeams">交换主客队</el-button>
              <el-button :icon="Delete" plain @click="clearSelection">清空</el-button>
            </div>

            <div class="selection-item">
              <label>分析视角</label>
              <el-radio-group v-model="analysisMode" class="mode-group">
                <el-radio-button label="balanced">均衡</el-radio-button>
                <el-radio-button label="aggressive">激进</el-radio-button>
                <el-radio-button label="conservative">稳健</el-radio-button>
              </el-radio-group>
            </div>

            <div class="quick-matchup-block">
              <div class="quick-title">快速对阵模板</div>
              <div class="quick-list">
                <el-button
                  v-for="preset in quickMatchups"
                  :key="preset.label"
                  size="small"
                  plain
                  @click="applyPreset(preset.home, preset.away)"
                >
                  {{ preset.label }}
                </el-button>
              </div>
            </div>

            <el-alert
              v-if="matchError"
              type="error"
              :title="matchError"
              show-icon
              closable
              style="margin-top: 16px"
            />

            <el-button
              class="predict-main-btn"
              type="primary"
              size="large"
              :loading="loadingMatchPrediction"
              @click="runMatchPrediction"
              :icon="Lightning"
            >
              分析比赛胜率
            </el-button>
          </div>
        </el-card>

        <el-card class="hint-card" shadow="never">
          <div class="hint-title">使用建议</div>
          <div class="hint-item">1. 先用模板快速选队，再根据需求微调。</div>
          <div class="hint-item">2. 情景推演可模拟主场优势变化对胜率的影响。</div>
          <div class="hint-item">3. 历史记录可复用近期预测，便于赛前对比。</div>
        </el-card>
      </el-col>

      <!-- 右侧：预测结果 -->
      <el-col :span="24" :lg="16">
        <el-card v-if="matchPrediction" class="result-card" shadow="always">
          <template #header>
            <div class="result-header">
              <span class="header-title">
                <el-icon><Trophy /></el-icon> 预测分析结果
              </span>
              <div class="confidence-tag" :class="confidenceTone">
                置信度: {{ percent(matchPrediction.confidence) }}%
              </div>
            </div>
          </template>

          <div class="top-kpis">
            <div class="kpi-card">
              <div class="kpi-label">预测胜方</div>
              <div class="kpi-value">{{ predictedWinner }}</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-label">胜率差</div>
              <div class="kpi-value">{{ Math.abs(homeWinPct - awayWinPct) }}%</div>
            </div>
            <div class="kpi-card">
              <div class="kpi-label">分析评分</div>
              <div class="kpi-value">{{ analysisScore }}</div>
            </div>
          </div>

          <div class="prediction-viz">
            <!-- 胜率对比条 -->
            <div class="vs-battle-container">
              <div class="team-meta home">
                <div class="team-avatar">{{ matchPrediction.home_team.charAt(0) }}</div>
                <div class="team-name">{{ matchPrediction.home_team }}</div>
                <div class="win-chance">{{ homeWinPct }}%</div>
              </div>

              <div class="vs-progress-wrapper">
                <div class="vs-progress-bar">
                  <div 
                    class="home-side" 
                    :style="{ width: homeWinPct + '%' }"
                  ></div>
                  <div 
                    class="away-side" 
                    :style="{ width: awayWinPct + '%' }"
                  ></div>
                </div>
                <div class="vs-indicator">VS</div>
              </div>

              <div class="team-meta away">
                <div class="win-chance">{{ awayWinPct }}%</div>
                <div class="team-name">{{ matchPrediction.away_team }}</div>
                <div class="team-avatar">{{ matchPrediction.away_team.charAt(0) }}</div>
              </div>
            </div>

            <el-divider>情景推演</el-divider>

            <div class="scenario-box">
              <div class="scenario-title">主场优势修正: {{ homeBoost }}%</div>
              <el-slider v-model="homeBoost" :min="-10" :max="10" :step="1" show-input size="small" />
              <div class="scenario-result">
                <span>修正后主队胜率: {{ adjustedHomePct }}%</span>
                <span>修正后客队胜率: {{ adjustedAwayPct }}%</span>
              </div>
              <div class="scenario-bar">
                <div class="scenario-home" :style="{ width: adjustedHomePct + '%' }"></div>
                <div class="scenario-away" :style="{ width: adjustedAwayPct + '%' }"></div>
              </div>
            </div>

            <!-- 详细指标 -->
            <el-divider>关键因子分析</el-divider>
            
            <div class="factors-grid">
              <div 
                v-for="factor in factorCards" 
                :key="factor.title"
                class="factor-card"
              >
                <el-icon class="factor-icon"><InfoFilled /></el-icon>
                <div class="factor-main">
                  <div class="factor-title">{{ factor.title }}</div>
                  <div class="factor-desc">{{ factor.description }}</div>
                </div>
              </div>
            </div>

            <el-divider>策略建议</el-divider>
            <div class="advice-list">
              <div v-for="tip in analysisTips" :key="tip" class="advice-item">
                <el-icon><ArrowRight /></el-icon>
                <span>{{ tip }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card v-else class="empty-result-card" shadow="never">
          <el-empty description="在左侧设置主客对阵，开启比赛胜率智能分析">
            <template #image>
              <el-icon :size="80" color="#dcdfe6"><TrendCharts /></el-icon>
            </template>
          </el-empty>
        </el-card>

        <el-card class="history-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><Connection /></el-icon> 最近预测记录
              </span>
              <el-button size="small" text @click="clearHistory" :disabled="predictionHistory.length === 0">清空记录</el-button>
            </div>
          </template>

          <el-empty v-if="predictionHistory.length === 0" description="暂无预测记录，先完成一次比赛预测" />

          <div v-else class="history-list">
            <div v-for="item in predictionHistory" :key="item.id" class="history-item">
              <div class="history-main">
                <div class="history-title">{{ item.homeTeam }} vs {{ item.awayTeam }}</div>
                <div class="history-meta">
                  <span>胜方: {{ item.winner }}</span>
                  <span>置信度: {{ item.confidence }}%</span>
                  <span>{{ item.time }}</span>
                </div>
              </div>
              <el-button size="small" plain @click="reuseHistory(item)">复用</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { 
  Connection, 
  HomeFilled, 
  LocationFilled, 
  Lightning, 
  Trophy, 
  InfoFilled, 
  TrendCharts,
  Refresh,
  Delete,
  ArrowRight
} from '@element-plus/icons-vue'
import { predictMatchOutcome } from '@/api/prediction'
import { getTeams } from '@/api/teams'
import type { MatchPredictionResult } from '@/api/types'

type AnalysisMode = 'balanced' | 'aggressive' | 'conservative'

type MatchHistoryItem = {
  id: string
  time: string
  homeTeam: string
  awayTeam: string
  winner: string
  confidence: number
}

type FactorCard = {
  title: string
  description: string
}

const HISTORY_KEY = 'nba.match.prediction.history.v1'

const homeTeam = ref('')
const awayTeam = ref('')
const teamOptions = ref<string[]>([])
const loadingTeams = ref(false)
const loadingMatchPrediction = ref(false)
const matchPrediction = ref<MatchPredictionResult | null>(null)
const matchError = ref('')
const analysisMode = ref<AnalysisMode>('balanced')
const homeBoost = ref(0)
const predictionHistory = ref<MatchHistoryItem[]>([])

const percent = (value: number) => Math.round(value * 100)

const homeWinPct = computed(() => percent(matchPrediction.value?.home_win_probability || 0))
const awayWinPct = computed(() => percent(matchPrediction.value?.away_win_probability || 0))

const confidenceTone = computed(() => {
  const c = matchPrediction.value?.confidence || 0
  if (c >= 0.8) return 'tone-high'
  if (c >= 0.65) return 'tone-mid'
  return 'tone-low'
})

const predictedWinner = computed(() => {
  if (!matchPrediction.value) return '-'
  return homeWinPct.value >= awayWinPct.value ? matchPrediction.value.home_team : matchPrediction.value.away_team
})

const analysisScore = computed(() => {
  if (!matchPrediction.value) return 0
  const gap = Math.abs(homeWinPct.value - awayWinPct.value)
  return Math.min(99, Math.round((matchPrediction.value.confidence * 70) + (gap * 0.3)))
})

const adjustedHomePct = computed(() => {
  if (!matchPrediction.value) return 0
  const base = matchPrediction.value.home_win_probability
  const modeBias = analysisMode.value === 'aggressive' ? 0.01 : analysisMode.value === 'conservative' ? -0.01 : 0
  const adjusted = Math.min(0.95, Math.max(0.05, base + (homeBoost.value / 100) + modeBias))
  return Math.round(adjusted * 100)
})

const adjustedAwayPct = computed(() => 100 - adjustedHomePct.value)

const quickMatchups = computed(() => {
  const source = teamOptions.value
  const presets = [
    ['Lakers', 'Warriors'],
    ['Celtics', 'Bucks'],
    ['Nuggets', 'Suns'],
    ['Mavericks', 'Thunder'],
  ]
  const existing = presets
    .filter(([home, away]) => source.includes(home) && source.includes(away))
    .map(([home, away]) => ({ home, away, label: `${home} vs ${away}` }))

  if (existing.length >= 3) {
    return existing
  }

  const fallback: Array<{ home: string; away: string; label: string }> = []
  for (let i = 0; i < source.length; i += 1) {
    for (let j = i + 1; j < source.length; j += 1) {
      fallback.push({
        home: source[i],
        away: source[j],
        label: `${source[i]} vs ${source[j]}`,
      })
      if (fallback.length >= 4) return fallback
    }
  }
  return fallback
})

const factorCards = computed<FactorCard[]>(() => {
  const factors = matchPrediction.value?.key_factors || []
  return factors.map((factor) => {
    const lower = factor.toLowerCase()
    if (lower.includes('主场')) {
      return { title: factor, description: '该因子会放大主队在主场条件下的基础优势。' }
    }
    if (lower.includes('防守')) {
      return { title: factor, description: '更好的防守效率通常让比赛结果更稳定。' }
    }
    if (lower.includes('胜率') || lower.includes('近期')) {
      return { title: factor, description: '近期状态会影响模型对短期走势的判断。' }
    }
    return { title: factor, description: '该因素参与了整体胜率打分。' }
  })
})

const analysisTips = computed(() => {
  if (!matchPrediction.value) {
    return ['请选择主客队并执行预测，系统会生成针对性的赛前建议。']
  }

  const tips: string[] = []
  const gap = Math.abs(homeWinPct.value - awayWinPct.value)
  const confidence = percent(matchPrediction.value.confidence)
  const winner = predictedWinner.value

  if (gap >= 20) {
    tips.push(`${winner} 当前优势较明显，可优先按主胜/客胜主方向解读。`)
  } else {
    tips.push('双方胜率接近，建议重点关注临场阵容和伤病名单。')
  }

  if (confidence >= 80) {
    tips.push('模型置信度较高，历史样本与当前形态一致性较强。')
  } else if (confidence >= 65) {
    tips.push('模型置信度中等，建议结合盘口和赛程密度二次确认。')
  } else {
    tips.push('模型置信度偏低，建议降低结论权重并增加人工判断。')
  }

  if (homeBoost.value !== 0) {
    tips.push(`当前已叠加 ${homeBoost.value}% 主场修正，可用于模拟临场心理和旅行疲劳影响。`)
  }

  return tips
})

const loadTeamOptions = async () => {
  loadingTeams.value = true
  try {
    const teams = await getTeams()
    teamOptions.value = Array.from(new Set(teams.map((team) => team.name).filter(Boolean)))
  } catch {
    teamOptions.value = []
  } finally {
    loadingTeams.value = false
  }
}

const applyPreset = (home: string, away: string) => {
  homeTeam.value = home
  awayTeam.value = away
  matchError.value = ''
}

const swapTeams = () => {
  const temp = homeTeam.value
  homeTeam.value = awayTeam.value
  awayTeam.value = temp
  matchError.value = ''
}

const clearSelection = () => {
  homeTeam.value = ''
  awayTeam.value = ''
  matchError.value = ''
  matchPrediction.value = null
  homeBoost.value = 0
}

const readHistory = () => {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    if (!raw) return
    const parsed = JSON.parse(raw) as MatchHistoryItem[]
    if (Array.isArray(parsed)) {
      predictionHistory.value = parsed.slice(0, 8)
    }
  } catch {
    predictionHistory.value = []
  }
}

const writeHistory = () => {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(predictionHistory.value.slice(0, 8)))
}

const appendHistory = (result: MatchPredictionResult) => {
  const item: MatchHistoryItem = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
    time: new Date().toLocaleString('zh-CN', { hour12: false }),
    homeTeam: result.home_team,
    awayTeam: result.away_team,
    winner: result.home_win_probability >= result.away_win_probability ? result.home_team : result.away_team,
    confidence: percent(result.confidence),
  }

  predictionHistory.value = [item, ...predictionHistory.value].slice(0, 8)
  writeHistory()
}

const clearHistory = () => {
  predictionHistory.value = []
  localStorage.removeItem(HISTORY_KEY)
}

const reuseHistory = (item: MatchHistoryItem) => {
  homeTeam.value = item.homeTeam
  awayTeam.value = item.awayTeam
  matchError.value = ''
}

const runMatchPrediction = async () => {
  if (!homeTeam.value || !awayTeam.value) {
    matchError.value = '请输入主队和客队名称后再预测'
    return
  }
  if (homeTeam.value === awayTeam.value) {
    matchError.value = '主队与客队不能相同'
    return
  }

  loadingMatchPrediction.value = true
  matchPrediction.value = null
  matchError.value = ''

  try {
    matchPrediction.value = await predictMatchOutcome({
      home_team: homeTeam.value,
      away_team: awayTeam.value,
    })
    homeBoost.value = 0
    appendHistory(matchPrediction.value)
  } catch (e) {
    const details = e instanceof Error ? `（${e.message}）` : ''
    matchError.value = `比赛预测失败：请检查后端接口${details}`
  } finally {
    loadingMatchPrediction.value = false
  }
}

onMounted(() => {
  loadTeamOptions()
  readHistory()
})
</script>

<style scoped>
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulseIndicator {
  0% { box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.1); }
  70% { box-shadow: 0 0 0 10px rgba(0, 0, 0, 0); }
  100% { box-shadow: 0 0 0 0 rgba(0, 0, 0, 0); }
}

.prediction-container {
  padding: 32px 24px;
  background: linear-gradient(120deg, #f0f4f8 0%, #e3e9f0 100%);
  min-height: calc(100vh - 84px);
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: #1a202c;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #718096;
  font-size: 16px;
}

.header-tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 选择卡片与全局卡片属性 */
.selection-card, .result-card, .history-card {
  border-radius: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.selection-card:hover, .history-card:hover {
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08);
}

.result-card {
  animation: fadeInUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) both;
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
  color: #2d3748;
}

.team-selection-box {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.selection-item label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 8px;
}

.custom-select {
  width: 100%;
}

.custom-select :deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  background: #f7fafc;
  transition: all 0.3s ease;
}
.custom-select :deep(.el-input__wrapper):hover, .custom-select :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #409eff inset, 0 4px 12px rgba(64,158,255,0.1);
}

.inline-actions {
  display: flex;
  gap: 8px;
}

.mode-group {
  display: flex;
  width: 100%;
}

.mode-group :deep(.el-radio-button__inner) {
  padding: 10px 16px;
  font-weight: 600;
}

.quick-matchup-block {
  border: 1px solid #edf2f7;
  border-radius: 12px;
  padding: 16px;
  background: linear-gradient(180deg, #fcfdff 0%, #f4f7f9 100%);
}

.quick-title {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
  margin-bottom: 10px;
}

.quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-list .el-tag {
  transition: all 0.2s ease;
  cursor: pointer;
  border-radius: 6px;
}
.quick-list .el-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

.vs-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.vs-divider::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #cbd5e0, transparent);
}

.vs-text {
  background: #fff;
  padding: 0 20px;
  z-index: 1;
  font-weight: 900;
  font-style: italic;
  color: #e2e8f0;
  font-size: 20px;
  text-shadow: 1px 1px 0px rgba(0,0,0,0.05);
}

.predict-main-btn {
  width: 100%;
  padding: 24px;
  border-radius: 14px;
  font-weight: 700;
  font-size: 18px;
  margin-top: 8px;
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.predict-main-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
}

.hint-card {
  margin-top: 20px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #f7fafc;
}

.hint-title {
  font-size: 14px;
  font-weight: 700;
  color: #4a5568;
  margin-bottom: 10px;
}

.hint-item {
  font-size: 13px;
  color: #718096;
  line-height: 1.8;
}

/* 结果展示区 */
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.confidence-tag {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.confidence-tag.tone-high {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #529b2e;
}

.confidence-tag.tone-mid {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #b88230;
}

.confidence-tag.tone-low {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #c45656;
}

.top-kpis {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  background: linear-gradient(145deg, #ffffff, #f8fafc);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
}

.kpi-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, rgba(64,158,255,0.1) 0%, transparent 70%);
  border-radius: 0 16px 0 60px;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.12);
  border-color: #c6e2ff;
}

.kpi-label {
  font-size: 13px;
  font-weight: 600;
  color: #718096;
}

.kpi-value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 900;
  background: linear-gradient(90deg, #1a202c, #4a5568);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* VS 可视化 */
.vs-battle-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 48px 0;
  padding: 0 20px;
}

.team-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 140px;
}

.team-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 900;
  color: white;
  box-shadow: 0 10px 20px rgba(0,0,0,0.15), inset 0 -4px 10px rgba(0,0,0,0.2);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.team-avatar:hover {
  transform: scale(1.1) rotate(5deg);
}

.home .team-avatar {
  background: linear-gradient(135deg, #C9082A 0%, #8b041b 100%);
  border: 3px solid #ffccd5;
}

.away .team-avatar {
  background: linear-gradient(135deg, #009fff 0%, #004b7f 100%);
  border: 3px solid #cce8ff;
}

.team-name {
  font-weight: 800;
  font-size: 18px;
  text-align: center;
  color: #2d3748;
}

.win-chance {
  font-size: 32px;
  font-weight: 900;
  color: #1a202c;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

.vs-progress-wrapper {
  flex: 1;
  margin: 0 48px;
  position: relative;
}

.vs-progress-bar {
  height: 32px;
  border-radius: 16px;
  display: flex;
  overflow: hidden;
  box-shadow: inset 0 2px 6px rgba(0,0,0,0.1), 0 4px 12px rgba(0,0,0,0.06);
  background: #edf2f7;
}

.home-side {
  background: linear-gradient(90deg, #ed213a 0%, #93291e 100%);
  transition: width 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.away-side {
  background: linear-gradient(90deg, #36D1DC 0%, #5B86E5 100%);
  transition: width 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.vs-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #ffffff;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 15px;
  color: #2d3748;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  border: 3px solid #e2e8f0;
  animation: pulseIndicator 3s infinite;
  z-index: 2;
}

.scenario-box {
  background: linear-gradient(180deg, #fafcff 0%, #f4f7f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.scenario-title {
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 12px;
}

.scenario-result {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
}

.scenario-bar {
  margin-top: 10px;
  height: 12px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  background: #e2e8f0;
}

.scenario-home {
  background: linear-gradient(90deg, #ed213a 0%, #93291e 100%);
  transition: width 0.3s ease;
}

.scenario-away {
  background: linear-gradient(90deg, #36D1DC 0%, #5B86E5 100%);
  transition: width 0.3s ease;
}

/* 因子分析 */
.factors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin: 32px 0;
}

.factor-card {
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 14px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  color: #4a5568;
  border: 1px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.factor-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 10px 24px rgba(0,0,0,0.08);
  border-color: #cbd5e0;
}

.factor-icon {
  color: #409eff;
  margin-top: 2px;
  background: #ebf5ff;
  padding: 8px;
  border-radius: 10px;
}

.factor-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.factor-title {
  color: #2d3748;
  font-weight: 700;
  font-size: 15px;
}

.factor-desc {
  font-size: 13px;
  color: #718096;
  line-height: 1.5;
}

.advice-list {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}

.advice-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  color: #4a5568;
  font-size: 14px;
  line-height: 1.7;
  padding: 10px 14px;
  background: #f7fafc;
  border-radius: 10px;
}

.empty-result-card {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.5);
  border: 2px dashed #cbd5e0;
  border-radius: 20px;
  transition: all 0.3s ease;
}
.empty-result-card:hover {
  border-color: #a0aec0;
  background-color: rgba(255, 255, 255, 0.8);
}

.history-list {
  display: grid;
  gap: 12px;
}

.history-item {
  border: 1px solid #edf2f7;
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #ffffff;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.history-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  border-color: #cbd5e0;
}

.history-main {
  min-width: 0;
}

.history-title {
  font-weight: 800;
  color: #2d3748;
  font-size: 15px;
}

.history-meta {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #718096;
}

@media (max-width: 1024px) {
  .top-kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .vs-battle-container {
    padding: 0;
    margin: 32px 0;
  }

  .vs-progress-wrapper {
    margin: 0 24px;
  }
}

@media (max-width: 640px) {
  .prediction-container {
    padding: 16px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }

  .top-kpis {
    grid-template-columns: 1fr;
  }

  .team-meta {
    width: auto;
  }

  .team-avatar {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .win-chance {
    font-size: 24px;
  }

  .history-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

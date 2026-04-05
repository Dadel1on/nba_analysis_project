
<template>
  <div class="prediction-container">
    <div class="page-header">
      <h1>比赛结果预测</h1>
      <p class="subtitle">基于球队近期状态、历史交锋及主客场因素的深度胜率分析</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：选择球队 -->
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
              <label>主队 (Home)</label>
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
              <label>客队 (Away)</label>
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
              icon="Lightning"
            >
              分析比赛胜率
            </el-button>
          </div>
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
              <div class="confidence-tag">
                置信度: {{ Math.round(matchPrediction.confidence * 100) }}%
              </div>
            </div>
          </template>

          <div class="prediction-viz">
            <!-- 胜率对比条 -->
            <div class="vs-battle-container">
              <div class="team-meta home">
                <div class="team-avatar">{{ matchPrediction.home_team.charAt(0) }}</div>
                <div class="team-name">{{ matchPrediction.home_team }}</div>
                <div class="win-chance">{{ Math.round(matchPrediction.home_win_probability * 100) }}%</div>
              </div>

              <div class="vs-progress-wrapper">
                <div class="vs-progress-bar">
                  <div 
                    class="home-side" 
                    :style="{ width: (matchPrediction.home_win_probability * 100) + '%' }"
                  ></div>
                  <div 
                    class="away-side" 
                    :style="{ width: (matchPrediction.away_win_probability * 100) + '%' }"
                  ></div>
                </div>
                <div class="vs-indicator">VS</div>
              </div>

              <div class="team-meta away">
                <div class="win-chance">{{ Math.round(matchPrediction.away_win_probability * 100) }}%</div>
                <div class="team-name">{{ matchPrediction.away_team }}</div>
                <div class="team-avatar">{{ matchPrediction.away_team.charAt(0) }}</div>
              </div>
            </div>

            <!-- 详细指标 -->
            <el-divider>关键因子分析</el-divider>
            
            <div class="factors-grid">
              <div 
                v-for="(factor, index) in matchPrediction.key_factors" 
                :key="index"
                class="factor-card"
              >
                <el-icon class="factor-icon"><InfoFilled /></el-icon>
                <span>{{ factor }}</span>
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
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { 
  Connection, 
  HomeFilled, 
  LocationFilled, 
  Lightning, 
  Trophy, 
  InfoFilled, 
  Warning,
  TrendCharts
} from '@element-plus/icons-vue'
import { predictMatchOutcome } from '@/api/prediction'
import { getTeams } from '@/api/teams'
import type { MatchPredictionResult } from '@/api/types'

const homeTeam = ref('')
const awayTeam = ref('')
const teamOptions = ref<string[]>([])
const loadingTeams = ref(false)
const loadingMatchPrediction = ref(false)
const matchPrediction = ref<MatchPredictionResult | null>(null)
const matchError = ref('')

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
  } catch (e) {
    const details = e instanceof Error ? `（${e.message}）` : ''
    matchError.value = `比赛预测失败：请检查后端接口${details}`
  } finally {
    loadingMatchPrediction.value = false
  }
}

onMounted(() => {
  loadTeamOptions()
})
</script>

<style scoped>
.prediction-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  margin-bottom: 32px;
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

/* 选择卡片 */
.selection-card {
  border-radius: 16px;
  border: none;
  background: #ffffff;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.team-selection-box {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.selection-item label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.custom-select {
  width: 100%;
}

.custom-select :deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
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
  background: #ebeef5;
}

.vs-text {
  background: #fff;
  padding: 0 16px;
  z-index: 1;
  font-weight: 800;
  font-style: italic;
  color: #dcdfe6;
  font-size: 18px;
}

.predict-main-btn {
  width: 100%;
  padding: 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 16px;
  margin-top: 10px;
}

/* 结果展示区 */
.result-card {
  border-radius: 16px;
  border: none;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.confidence-tag {
  background: #f0f9eb;
  color: #67c23a;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

/* VS 可视化 */
.vs-battle-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 40px 0;
  padding: 0 20px;
}

.team-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 120px;
}

.team-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: white;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.home .team-avatar {
  background: linear-gradient(135deg, #C9082A 0%, #a00621 100%);
}

.away .team-avatar {
  background: linear-gradient(135deg, #006BB6 0%, #004b7f 100%);
}

.team-name {
  font-weight: 700;
  font-size: 16px;
  text-align: center;
}

.win-chance {
  font-size: 24px;
  font-weight: 800;
  color: #303133;
}

.vs-progress-wrapper {
  flex: 1;
  margin: 0 40px;
  position: relative;
}

.vs-progress-bar {
  height: 24px;
  border-radius: 12px;
  display: flex;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.1);
}

.home-side {
  background: #C9082A;
  transition: width 1s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.away-side {
  background: #006BB6;
  transition: width 1s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.vs-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  border: 2px solid #fff;
}

/* 因子分析 */
.factors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin: 24px 0;
}

.factor-card {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #606266;
  border: 1px solid #edf2f7;
}

.factor-icon {
  color: #409eff;
}

.empty-result-card {
  height: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  border: 2px dashed #e4e7ed;
}
</style>

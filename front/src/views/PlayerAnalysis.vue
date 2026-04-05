<template>
  <div class="player-analysis-container">
    <div class="page-header">
      <div class="header-content">
        <h1>球员中心</h1>
        <p class="subtitle">球员数据探查与表现预测一体化工作台</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="player-tabs" stretch>
      <el-tab-pane label="数据探查" name="browse">
        <el-card shadow="never" class="search-card">
          <div class="search-wrapper">
            <el-input 
              v-model="searchQuery" 
              placeholder="输入球员姓名进行搜索 (例如: LeBron James)" 
              class="custom-search-input"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" class="search-btn" :icon="Search" @click="handleSearch" :loading="loading">
              立即搜索
            </el-button>
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

        <el-card shadow="always" class="list-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon><User /></el-icon> 球员名录
              </span>
              <span v-if="players.length" class="result-count">找到 {{ players.length }} 名球员</span>
            </div>
          </template>

          <el-table 
            v-loading="loading"
            :data="players" 
            style="width: 100%" 
            class="modern-table"
            row-class-name="player-row"
          >
            <el-table-column label="球员信息" min-width="250">
              <template #default="scope">
                <div class="player-info-cell">
                  <div class="player-avatar-mini" :style="{ backgroundColor: getTeamColor(scope.row.team) }">
                    {{ scope.row.name.charAt(0) }}
                  </div>
                  <div class="player-meta">
                    <span class="p-name-main">{{ scope.row.name }}</span>
                    <span v-if="scope.row.position" class="p-position-sub">{{ scope.row.position }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="所属球队" min-width="180">
              <template #default="scope">
                <div class="team-tag-wrapper">
                  <el-tag :type="scope.row.team ? 'info' : 'warning'" effect="plain" round size="small">
                    {{ scope.row.team || '自由球员' }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="场均数据 (得/篮/助)" min-width="220" align="center">
              <template #default="scope">
                <div class="stats-badges">
                  <el-tooltip content="场均得分" placement="top">
                    <span class="stat-badge pts">{{ format2(scope.row.stats.points) }}</span>
                  </el-tooltip>
                  <el-tooltip content="场均篮板" placement="top">
                    <span class="stat-badge reb">{{ format2(scope.row.stats.rebounds) }}</span>
                  </el-tooltip>
                  <el-tooltip content="场均助攻" placement="top">
                    <span class="stat-badge ast">{{ format2(scope.row.stats.assists) }}</span>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  link 
                  class="view-btn"
                  @click="viewDetails(scope.row)"
                >
                  详情分析 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!loading && !players.length" class="empty-state">
            <el-empty description="未找到相关球员数据，请尝试其他关键词" />
          </div>

          <div v-if="players.length > 0" class="pagination-footer">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="表现预测" name="predict">
        <PredictionView embedded />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LocationQueryRaw } from 'vue-router'
import { Search, User, ArrowRight } from '@element-plus/icons-vue'
import { searchPlayers } from '@/api/players'
import type { PlayerSummary } from '@/api/types'
import PredictionView from './Prediction.vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref<'browse' | 'predict'>(
  route.query.tab === 'predict' || route.query.tab === 'prediction' ? 'predict' : 'browse'
)
const searchQuery = ref('')
const players = ref<PlayerSummary[]>([])
const loading = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0) // Note: Backend searchPlayers currently doesn't return total count in the payload, but we'll use a placeholder or update it later.

const getTeamColor = (teamName: string | null) => {
  if (!teamName) return '#909399'
  const colors: Record<string, string> = {
    '湖人': '#552583',
    '勇士': '#1D428A',
    '76人': '#006BB6',
    '独行侠': '#00538C',
    '雷霆': '#007AC1',
    '雄鹿': '#00471B',
    '掘金': '#0E2240',
    '凯尔特人': '#007A33',
    '太阳': '#1D1160',
    '快船': '#C8102E'
  }
  return colors[teamName] || '#909399'
}

const format2 = (value: unknown) => {
  const num = Number(value)
  return Number.isFinite(num) ? num.toFixed(2) : '-'
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPlayers()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchPlayers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchPlayers()
}

const fetchPlayers = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await searchPlayers({
      name: searchQuery.value,
      page: currentPage.value,
      limit: pageSize.value,
    })
    players.value = data
    // Total count is currently not returned by API as a separate field in searchPlayers, 
    // assuming it might be added or we can just use the length for now if it's small.
    // In a real scenario, the API should return { list, total }.
    total.value = data.length < pageSize.value ? (currentPage.value - 1) * pageSize.value + data.length : 1000 
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载球员数据失败'
    players.value = []
  } finally {
    loading.value = false
  }
}

const viewDetails = (player: PlayerSummary) => {
  router.push(`/players/${player.id}`)
}

onMounted(() => {
  if (activeTab.value === 'browse') fetchPlayers()
})

watch(
  () => route.query.tab,
  (tab) => {
    const next = tab === 'predict' || tab === 'prediction' ? 'predict' : 'browse'
    if (activeTab.value !== next) activeTab.value = next
  }
)

watch(activeTab, (tab) => {
  const desired = tab === 'predict' ? 'predict' : undefined
  const current = typeof route.query.tab === 'string' ? route.query.tab : undefined
  const isSame = desired === current || (desired === undefined && current === undefined)
  if (isSame) return

  const query: LocationQueryRaw = { ...route.query }
  if (desired) query.tab = desired
  else delete query.tab
  router.replace({ path: route.path, query })

  if (tab === 'browse' && !loading.value && players.value.length === 0) fetchPlayers()
})
</script>

<style scoped>
.player-analysis-container {
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

/* 搜索卡片 */
.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.search-wrapper {
  display: flex;
  gap: 16px;
}

.custom-search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 16px;
  font-size: 15px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
}

.search-btn {
  padding: 0 32px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
}

.error-alert {
  margin-bottom: 20px;
  border-radius: 12px;
}

/* 列表卡片 */
.list-card {
  border-radius: 16px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 17px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.result-count {
  font-size: 13px;
  color: #909399;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 700;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 0;
}

.modern-table :deep(.el-table__row) {
  height: 72px;
}

.player-info-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.player-avatar-mini {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 800;
  font-size: 18px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.player-meta {
  display: flex;
  flex-direction: column;
}

.p-name-main {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
}

.p-position-sub {
  font-size: 12px;
  color: #909399;
}

.team-tag-wrapper {
  display: flex;
  align-items: center;
}

.stats-badges {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.stat-badge {
  display: inline-block;
  min-width: 44px;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 13px;
  text-align: center;
}

.stat-badge.pts { background: #fef2f2; color: #C9082A; }
.stat-badge.reb { background: #f0f9eb; color: #67C23A; }
.stat-badge.ast { background: #ecf5ff; color: #409EFF; }

.view-btn {
  font-weight: 600;
  font-size: 14px;
}

.pagination-footer {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.empty-state {
  padding: 40px 0;
}
</style>

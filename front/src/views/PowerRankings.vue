
<template>
  <div class="power-rankings-container">
    <div class="page-header">
      <div class="header-content">
        <h1>球员战力榜单</h1>
        <p class="subtitle">实时更新联盟顶级得分手排名，基于场均得分（PPG）深度分析</p>
      </div>
      <el-button @click="router.back()" icon="ArrowLeft" round>返回仪表盘</el-button>
    </div>

    <!-- 前三名领奖台展示 -->
    <div v-if="currentPage === 1 && rankings.length >= 3" class="podium-container">
      <div class="podium-item silver">
        <div class="rank-badge">2</div>
        <div class="player-avatar-large">{{ rankings[1].name.charAt(0) }}</div>
        <div class="player-name">{{ rankings[1].name }}</div>
        <div class="player-team">{{ rankings[1].team }}</div>
        <div class="player-score">{{ rankings[1].points }} <span class="unit">PPG</span></div>
      </div>
      <div class="podium-item gold">
        <div class="crown-icon">👑</div>
        <div class="rank-badge">1</div>
        <div class="player-avatar-large">{{ rankings[0].name.charAt(0) }}</div>
        <div class="player-name">{{ rankings[0].name }}</div>
        <div class="player-team">{{ rankings[0].team }}</div>
        <div class="player-score">{{ rankings[0].points }} <span class="unit">PPG</span></div>
      </div>
      <div class="podium-item bronze">
        <div class="rank-badge">3</div>
        <div class="player-avatar-large">{{ rankings[2].name.charAt(0) }}</div>
        <div class="player-name">{{ rankings[2].name }}</div>
        <div class="player-team">{{ rankings[2].team }}</div>
        <div class="player-score">{{ rankings[2].points }} <span class="unit">PPG</span></div>
      </div>
    </div>

    <el-card shadow="always" class="rankings-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><Histogram /></el-icon> 战力详情列表
          </span>
        </div>
      </template>

      <el-table 
        :data="rankings" 
        v-loading="loading" 
        style="width: 100%" 
        class="modern-table"
        :row-class-name="tableRowClassName"
      >
        <el-table-column label="排名" width="100" align="center">
          <template #default="scope">
            <div :class="['rank-circle', 'rank-' + ((currentPage - 1) * pageSize + scope.$index + 1)]">
              {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="球员信息" min-width="250">
          <template #default="scope">
            <div class="player-info-cell">
              <div class="player-avatar-mini" :style="{ backgroundColor: getTeamColor(scope.row.team) }">
                {{ scope.row.name.charAt(0) }}
              </div>
              <div class="player-meta">
                <span class="p-name-main">{{ scope.row.name }}</span>
                <span class="p-team-sub">{{ scope.row.team }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数据表现" min-width="200" align="right">
          <template #default="scope">
            <div class="stats-display">
              <div class="ppg-modern">{{ scope.row.points }} <span class="unit">PPG</span></div>
              <el-progress 
                :percentage="Math.min((scope.row.points / 35) * 100, 100)" 
                :show-text="false" 
                :stroke-width="4"
                color="#C9082A"
                class="ppg-progress"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Histogram } from '@element-plus/icons-vue'
import { getPowerRankings } from '@/api/dashboard'
import type { TopPlayer } from '@/api/types'

const router = useRouter()
const rankings = ref<TopPlayer[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getTeamColor = (teamName: string) => {
  const colors: Record<string, string> = {
    '76人': '#006BB6',
    '独行侠': '#00538C',
    '雷霆': '#007AC1',
    '雄鹿': '#00471B',
    '掘金': '#0E2240',
    '凯尔特人': '#007A33',
    '湖人': '#552583',
    '勇士': '#1D428A',
    '太阳': '#1D1160',
    '快船': '#C8102E'
  }
  return colors[teamName] || '#909399'
}

const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  if (currentPage.value === 1 && rowIndex < 3) {
    return 'top-rank-row'
  }
  return ''
}

const fetchRankings = async () => {
  loading.value = true
  try {
    const result = await getPowerRankings(currentPage.value, pageSize.value)
    rankings.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchRankings()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchRankings()
}

onMounted(() => {
  fetchRankings()
})
</script>

<style scoped>
.power-rankings-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #606266;
  font-size: 16px;
}

/* 领奖台样式 */
.podium-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(180deg, rgba(201, 8, 42, 0.05) 0%, rgba(255, 255, 255, 0) 100%);
  border-radius: 24px;
}

.podium-item {
  flex: 1;
  max-width: 220px;
  background: white;
  border-radius: 20px;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: transform 0.3s ease;
}

.podium-item:hover {
  transform: translateY(-10px);
}

.podium-item.gold {
  height: 320px;
  border-top: 6px solid #ffd700;
  z-index: 2;
  box-shadow: 0 15px 40px rgba(255, 215, 0, 0.15);
}

.podium-item.silver {
  height: 280px;
  border-top: 6px solid #c0c0c0;
}

.podium-item.bronze {
  height: 260px;
  border-top: 6px solid #cd7f32;
}

.crown-icon {
  position: absolute;
  top: -25px;
  font-size: 30px;
}

.rank-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 28px;
  height: 28px;
  background: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
  color: #909399;
}

.player-avatar-large {
  width: 80px;
  height: 80px;
  background: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 900;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.gold .player-avatar-large { background: #fff9e6; color: #b8860b; }
.silver .player-avatar-large { background: #f5f5f5; color: #7f8c8d; }
.bronze .player-avatar-large { background: #fff2e6; color: #a0522d; }

.player-name {
  font-size: 18px;
  font-weight: 800;
  color: #1a1a1a;
  text-align: center;
  margin-bottom: 4px;
}

.player-team {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}

.player-score {
  font-size: 24px;
  font-weight: 900;
  color: #C9082A;
}

.player-score .unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

/* 卡片与表格样式 */
.rankings-card {
  border-radius: 20px;
  border: none;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8f9fa;
  color: #909399;
  font-weight: 700;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 1px;
  padding: 16px 0;
}

.modern-table :deep(.el-table__row) {
  height: 80px;
}

.player-info-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.player-avatar-mini {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 800;
  font-size: 18px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.player-meta {
  display: flex;
  flex-direction: column;
}

.p-name-main {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
}

.p-team-sub {
  font-size: 13px;
  color: #909399;
}

.stats-display {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.ppg-modern {
  font-size: 22px;
  font-weight: 900;
  color: #1a1a1a;
  line-height: 1;
}

.ppg-modern .unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

.ppg-progress {
  width: 100px;
}

.rank-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #606266;
  background: #f0f2f5;
  font-size: 15px;
}

.rank-1 { background: #ffd700; color: #fff; box-shadow: 0 4px 10px rgba(255, 215, 0, 0.4); }
.rank-2 { background: #c0c0c0; color: #fff; box-shadow: 0 4px 10px rgba(192, 192, 192, 0.4); }
.rank-3 { background: #cd7f32; color: #fff; box-shadow: 0 4px 10px rgba(205, 127, 50, 0.4); }

.pagination-container {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}
</style>

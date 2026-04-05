
<template>
  <div class="game-records-container">
    <div class="page-header">
      <div class="header-content">
        <h1>历史比赛记录</h1>
        <p class="subtitle">全面回顾 NBA 历史对阵数据与比分结果</p>
      </div>
      <el-button @click="router.back()" icon="ArrowLeft">返回仪表盘</el-button>
    </div>

    <el-card shadow="never" class="records-card">
      <el-table :data="games" v-loading="loading" style="width: 100%" class="modern-table">
        <el-table-column prop="date" label="比赛日期" width="150" align="center">
          <template #default="scope">
            <span class="date-cell">{{ scope.row.date }}</span>
          </template>
        </el-table-column>
        <el-table-column label="对阵详情" min-width="320" align="center">
          <template #default="scope">
            <div class="match-display">
              <div class="team-mini home">
                <span class="t-name">{{ scope.row.homeTeam }}</span>
                <span class="team-initial">{{ scope.row.homeTeam.charAt(0) }}</span>
              </div>
              <span class="vs-label">vs</span>
              <div class="team-mini away">
                <span class="team-initial away">{{ scope.row.awayTeam.charAt(0) }}</span>
                <span class="t-name">{{ scope.row.awayTeam }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="比分结果" width="150" align="center">
          <template #default="scope">
            <div class="score-badge" :class="{ 'home-win': isHomeWin(scope.row.score), 'away-win': isAwayWin(scope.row.score) }">
              {{ scope.row.score }}
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
import { ArrowLeft } from '@element-plus/icons-vue'
import { getGameRecords } from '@/api/dashboard'
import type { RecentGame } from '@/api/types'

const router = useRouter()
const games = ref<RecentGame[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const isHomeWin = (score: string) => {
  const match = score.match(/(\d+)\s*-\s*(\d+)/)
  if (match) {
    const h = Number(match[1])
    const a = Number(match[2])
    return h > a
  }
  return false
}

const isAwayWin = (score: string) => {
  const match = score.match(/(\d+)\s*-\s*(\d+)/)
  if (match) {
    const h = Number(match[1])
    const a = Number(match[2])
    return a > h
  }
  return false
}

const fetchGames = async () => {
  loading.value = true
  try {
    const result = await getGameRecords(currentPage.value, pageSize.value)
    games.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchGames()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchGames()
}

onMounted(() => {
  fetchGames()
})
</script>

<style scoped>
.game-records-container {
  padding: 24px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 84px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.records-card {
  border-radius: 16px;
  border: none;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.date-cell {
  color: #909399;
  font-weight: 500;
  font-size: 14px;
}

.match-display {
  display: flex;
  align-items: center;
  justify-content: center;
}

.team-mini {
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-mini.home {
  width: 140px;
  justify-content: flex-end;
}

.team-mini.away {
  width: 140px;
  justify-content: flex-start;
}

.team-initial {
  width: 28px;
  height: 28px;
  background: #C9082A;
  color: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
}

.team-initial.away {
  background: #006BB6;
}

.t-name {
  font-weight: 600;
  color: #303133;
}

.vs-label {
  font-size: 12px;
  font-weight: 800;
  color: #c0c4cc;
  font-style: italic;
  width: 32px;
  text-align: center;
  flex-shrink: 0;
}

.score-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #f4f4f5;
  color: #909399;
  border-radius: 6px;
  font-weight: 700;
  font-family: 'Monaco', monospace;
}

.score-badge.home-win {
  background: #fef2f2;
  color: #C9082A;
}

.score-badge.away-win {
  background: #f0f9eb;
  color: #67C23A;
}
</style>

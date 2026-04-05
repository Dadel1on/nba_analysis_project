<template>
  <div class="player-detail">
    <div class="header-row">
      <div>
        <h1>球员详情</h1>
        <p class="muted">用于承接球员数据探查的详情钻取页面。</p>
      </div>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon />

    <el-skeleton v-if="loading" :rows="6" animated style="margin-top: 16px" />

    <el-card v-else-if="player" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>{{ player.name }}</span>
          <el-tag>{{ player.position || '未知位置' }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="球员ID">{{ player.id }}</el-descriptions-item>
        <el-descriptions-item label="球队">{{ player.team || '未知球队' }}</el-descriptions-item>
        <el-descriptions-item label="最近赛季场均得分">{{ player.stats.points?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="最近赛季场均篮板">{{ player.stats.rebounds?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="最近赛季场均助攻">{{ player.stats.assists?.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-else description="未查询到该球员数据" style="margin-top: 24px" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPlayerById, type PlayerSummary } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const errorMessage = ref('')
const player = ref<PlayerSummary | null>(null)

const goBack = () => {
  router.push('/players')
}

const loadPlayer = async () => {
  const playerId = Number(route.params.id)
  if (!Number.isFinite(playerId) || playerId <= 0) {
    errorMessage.value = '无效的球员ID'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    player.value = await getPlayerById(playerId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载球员详情失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPlayer()
})
</script>

<style scoped>
.player-detail {
  padding: 12px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.muted {
  margin: 0;
  color: #909399;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>

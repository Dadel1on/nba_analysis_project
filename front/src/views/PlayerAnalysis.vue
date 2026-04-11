<template>
  <div class="player-analysis-container">
    <div class="page-header">
      <div class="header-content">
        <h1>表现预测中心</h1>
        <p class="subtitle">聚焦球员下一场表现预测与分析建议</p>
      </div>
    </div>

    <PredictionView embedded />
  </div>
</template>

<script setup lang="ts">
import PredictionView from './Prediction.vue'
</script>

<style scoped>
.player-analysis-container {
  padding: 0;
  background-color: transparent;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: #1f2a44;
  margin: 0 0 10px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #606266;
  font-size: 16px;
  margin: 0;
}
</style>

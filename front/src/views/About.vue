<template>
  <div class="about-page">
    <header class="page-header">
      <h1 class="title">项目架构与 <span class="gradient-text">核心技术</span></h1>
      <p class="subtitle">前后端分离 · 大数据离线计算 · 可视化分析</p>
      <div class="badge-row">
        <span class="badge">Vue 3</span>
        <span class="badge">Spring Boot</span>
        <span class="badge">Spark</span>
        <span class="badge">ECharts</span>
      </div>
    </header>

    <section class="card card-wide">
      <div class="card-tag">System Architecture</div>
      <h3 class="card-title">全栈架构设计</h3>
      <p class="card-desc">系统采用前后端分离架构，通过 RESTful API 进行数据交互，兼顾开发效率与可扩展性。</p>
      <div class="arch-flow">
        <div class="flow-item">
          <div class="icon-box">Vue 3</div>
          <span class="flow-label">前端展示层</span>
        </div>
        <div class="flow-arrow">→</div>
        <div class="flow-item">
          <div class="icon-box">Spring Boot</div>
          <span class="flow-label">业务逻辑层</span>
        </div>
        <div class="flow-arrow">→</div>
        <div class="flow-item">
          <div class="icon-box">MySQL / Spark</div>
          <span class="flow-label">数据存储与计算</span>
        </div>
      </div>
    </section>

    <div class="grid">
      <section class="card">
        <div class="card-tag">Technology Stack</div>
        <h3 class="card-title">核心技术选型</h3>
        <div class="tech-list">
          <div class="tech-group">
            <h4 class="group-title">Frontend</h4>
            <ul class="list">
              <li>Vue 3 (Composition API)</li>
              <li>Element Plus UI</li>
              <li>ECharts 6.0 可视化</li>
              <li>Tailwind CSS 布局</li>
            </ul>
          </div>
          <div class="tech-group">
            <h4 class="group-title">Backend</h4>
            <ul class="list">
              <li>Spring Boot 3.x</li>
              <li>Spring Data JPA</li>
              <li>Apache Spark 离线计算</li>
              <li>RESTful API 设计</li>
            </ul>
          </div>
        </div>
      </section>

      <section class="card">
        <div class="card-tag">AI & Big Data</div>
        <h3 class="card-title">Spark 智能分析逻辑</h3>
        <p class="card-desc">利用 Spark 的分布式计算能力，对海量球员历史数据进行清洗、建模与批量预测。</p>
        <ul class="logic-list">
          <li><span class="key">特征提取</span>提取得分、篮板、助攻、命中率等核心指标。</li>
          <li><span class="key">价值评估</span>结合进阶数据进行加权计算，输出可解释的综合指标。</li>
          <li><span class="key">趋势预测</span>基于历史表现曲线预测未来赛场表现并可视化展示。</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<style scoped>
 .about-page {
  max-width: 1200px;
  margin: 0 auto;
  color: #000;
}

.page-header {
  margin-bottom: 24px;
}

.title {
  font-size: 34px;
  font-weight: 900;
  letter-spacing: -1px;
  margin: 0 0 10px;
}

.gradient-text {
  background: linear-gradient(90deg, #5252ff, #7a7aff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  margin: 0 0 14px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.55);
}

.badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: rgba(82, 82, 255, 0.08);
  border: 1px solid rgba(82, 82, 255, 0.12);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #5252ff;
  letter-spacing: 0.5px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 24px;
  padding: 24px;
  backdrop-filter: blur(16px);
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.04);
}

.card-wide {
  margin-bottom: 24px;
}

.card-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: rgba(0, 0, 0, 0.55);
  background: rgba(0, 0, 0, 0.03);
  margin-bottom: 14px;
}

.card-title {
  font-size: 18px;
  font-weight: 800;
  margin: 0 0 10px;
  color: #000;
}

.card-desc {
  margin: 0 0 16px;
  color: rgba(0, 0, 0, 0.62);
  line-height: 1.7;
  font-size: 14px;
}

.arch-flow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.flow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 160px;
}

.icon-box {
  width: 120px;
  height: 70px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: #000;
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.flow-label {
  font-size: 12px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.45);
}

.flow-arrow {
  color: rgba(0, 0, 0, 0.18);
  font-weight: 900;
  font-size: 18px;
}

.tech-list {
  display: flex;
  gap: 22px;
}

.tech-group {
  flex: 1;
}

.group-title {
  margin: 0 0 10px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 1px;
  color: rgba(0, 0, 0, 0.55);
  text-transform: uppercase;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.68);
  font-weight: 600;
}

.list li:last-child {
  border-bottom: none;
}

.list li::before {
  content: "";
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(82, 82, 255, 0.45);
}

.logic-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.logic-list li {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.04);
  color: rgba(0, 0, 0, 0.68);
  line-height: 1.6;
  font-weight: 600;
}

.key {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  margin-right: 10px;
  font-size: 12px;
  font-weight: 800;
  color: #5252ff;
  background: rgba(82, 82, 255, 0.08);
  border: 1px solid rgba(82, 82, 255, 0.12);
}

@media (max-width: 992px) {
  .grid { grid-template-columns: 1fr; }
  .arch-flow { flex-direction: column; align-items: stretch; }
  .flow-item { min-width: auto; }
  .flow-arrow { align-self: center; transform: rotate(90deg); }
}
</style>

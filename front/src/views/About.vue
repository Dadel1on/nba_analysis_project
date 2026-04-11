<template>
  <div class="about-page fade-in">
    <header class="page-header slide-up">
      <p class="eyebrow">ABOUT THIS PROJECT</p>
      <h1 class="title">NBA 数据分析平台 <span class="gradient-text">项目全景</span></h1>
      <p class="subtitle">
        从历史比赛数据采集、离线指标计算，到前端交互分析与比赛预测，构建完整的数据驱动决策链路。
      </p>
      <div class="badge-row">
        <span class="badge">Vue 3</span>
        <span class="badge">Spring Boot</span>
        <span class="badge">Spark</span>
        <span class="badge">MariaDB / MySQL</span>
        <span class="badge">ECharts</span>
      </div>
      <div class="stat-row">
        <div class="stat-item">
          <div class="stat-value">4+</div>
          <div class="stat-label">核心业务模块</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">10+</div>
          <div class="stat-label">可视化分析页面</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">3 层</div>
          <div class="stat-label">数据加工分层</div>
        </div>
      </div>
    </header>

    <section class="card card-wide slide-up" style="--delay: 0.1s">
      <div class="card-tag">Project Positioning</div>
      <h3 class="card-title">面向分析、预测与教学演示的实战型系统</h3>
      <p class="card-desc">
        平台不仅关注单一榜单展示，更强调数据处理过程透明、指标可追踪、业务结论可解释。
      </p>
      <div class="position-grid">
        <article class="position-item">
          <h4>可解释数据指标</h4>
          <p>将基础数据与进阶指标统一建模，支持从球员表现到球队对比的多维解读。</p>
        </article>
        <article class="position-item">
          <h4>离线计算可扩展</h4>
          <p>通过 Spark 脚本与 SQL 分阶段计算，便于后续接入新赛季数据和新特征。</p>
        </article>
        <article class="position-item">
          <h4>前端分析可交互</h4>
          <p>围绕图表联动、条件筛选和模块化页面设计，让数据洞察直观可用。</p>
        </article>
      </div>
    </section>

    <section class="card card-wide slide-up" style="--delay: 0.2s">
      <div class="card-tag">Data Pipeline</div>
      <h3 class="card-title">端到端数据闭环流程</h3>
      <div class="pipeline">
        <div class="step-item">
          <span class="step-no">01</span>
          <div class="step-main">
            <h4>原始数据接入</h4>
            <p>采集 Kaggle 历史数据与球员比赛记录，完成字段映射与统一命名。</p>
          </div>
        </div>
        <div class="step-item">
          <span class="step-no">02</span>
          <div class="step-main">
            <h4>数据清洗与标准化</h4>
            <p>处理缺失值、异常值、时间字段与主外键关系，确保后续计算稳定。</p>
          </div>
        </div>
        <div class="step-item">
          <span class="step-no">03</span>
          <div class="step-main">
            <h4>Spark 批量特征计算</h4>
            <p>离线计算进攻效率、综合表现指数、趋势特征等核心分析指标。</p>
          </div>
        </div>
        <div class="step-item">
          <span class="step-no">04</span>
          <div class="step-main">
            <h4>数据库回填与服务封装</h4>
            <p>结果入库后由 Spring Boot 提供统一 REST API，支持分页、筛选和统计查询。</p>
          </div>
        </div>
        <div class="step-item">
          <span class="step-no">05</span>
          <div class="step-main">
            <h4>前端图表分析与预测展示</h4>
            <p>通过可视化视图实现球员分析、球队对比、实力榜和比赛预测结果展示。</p>
          </div>
        </div>
      </div>
    </section>

    <div class="grid">
      <section class="card slide-up" style="--delay: 0.3s">
        <div class="card-tag">Technology Stack</div>
        <h3 class="card-title">核心技术选型</h3>
        <div class="tech-list">
          <div class="tech-group">
            <h4 class="group-title">Frontend</h4>
            <ul class="list">
              <li>Vue 3 + TypeScript</li>
              <li>Element Plus 组件体系</li>
              <li>ECharts 可视化渲染</li>
              <li>Vite 构建与模块化开发</li>
            </ul>
          </div>
          <div class="tech-group">
            <h4 class="group-title">Backend</h4>
            <ul class="list">
              <li>Spring Boot 3.x</li>
              <li>Spring Data JPA</li>
              <li>分层服务与统一异常处理</li>
              <li>RESTful API 契约化输出</li>
            </ul>
          </div>
          <div class="tech-group">
            <h4 class="group-title">Data</h4>
            <ul class="list">
              <li>Apache Spark 离线任务</li>
              <li>SQL 脚本分阶段加工</li>
              <li>维度增强与指标回填</li>
              <li>结构化数据长期复用</li>
            </ul>
          </div>
        </div>
      </section>

      <section class="card slide-up" style="--delay: 0.4s">
        <div class="card-tag">Product Capabilities</div>
        <h3 class="card-title">功能矩阵覆盖</h3>
        <ul class="logic-list">
          <li><span class="key">球员分析</span>支持单球员详情、能力结构拆解、赛季表现对比。</li>
          <li><span class="key">球队对比</span>按多维指标比较球队攻防表现与稳定性。</li>
          <li><span class="key">实力排行</span>基于综合指数形成榜单，辅助快速定位强弱队伍。</li>
          <li><span class="key">比赛预测</span>输入对阵条件后输出胜率趋势与关键影响因素。</li>
          <li><span class="key">数据管理</span>支持离线任务执行与处理结果核验。</li>
        </ul>
      </section>

      <section class="card slide-up" style="--delay: 0.5s">
        <div class="card-tag">Engineering Practice</div>
        <h3 class="card-title">工程化与可维护性</h3>
        <ul class="list">
          <li>前后端 API 合同文档化，减少联调歧义。</li>
          <li>异常处理集中化，统一返回结构和错误提示。</li>
          <li>脚本化数据加工流程，便于环境迁移与复跑。</li>
          <li>页面按业务领域拆分，提升功能迭代效率。</li>
        </ul>
      </section>

      <section class="card slide-up" style="--delay: 0.6s">
        <div class="card-tag">Roadmap</div>
        <h3 class="card-title">后续迭代方向</h3>
        <div class="roadmap">
          <div class="roadmap-item">
            <span class="dot"></span>
            <div>
              <h4>短期</h4>
              <p>完善模型评估指标与预测结果解释面板，增强业务可信度。</p>
            </div>
          </div>
          <div class="roadmap-item">
            <span class="dot"></span>
            <div>
              <h4>中期</h4>
              <p>引入更多赛季与伤病、赛程密度等上下文特征，提高预测稳定性。</p>
            </div>
          </div>
          <div class="roadmap-item">
            <span class="dot"></span>
            <div>
              <h4>长期</h4>
              <p>探索近实时更新链路与自动化训练调度，逐步向智能分析平台演进。</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  --about-bg: #f9fafb;
  --about-card: #ffffff;
  --about-line: #f3f4f6;
  --about-text: #111827;
  --about-subtle: #6b7280;
  --about-accent: #4f46e5;
  --about-accent-soft: rgba(79, 70, 229, 0.1);
  --about-accent-border: rgba(79, 70, 229, 0.2);
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  color: var(--about-text);
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  position: relative;
}

.about-page::before {
  content: "";
  position: fixed;
  inset: 0;
  background:
    radial-gradient(circle at 10% 0%, rgba(79, 70, 229, 0.06), transparent 35%),
    radial-gradient(circle at 90% 20%, rgba(14, 165, 233, 0.05), transparent 40%),
    var(--about-bg);
  z-index: -1;
}

.page-header {
  margin-bottom: 40px;
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 13px;
  letter-spacing: 1.5px;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
}

.title {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.025em;
  margin: 0 0 16px;
  color: #111827;
}

.gradient-text {
  background: linear-gradient(135deg, #4f46e5, #0ea5e9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  margin: 0 0 24px;
  font-size: 16px;
  color: var(--about-subtle);
  max-width: 900px;
  line-height: 1.7;
}

.badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  background: var(--about-accent-soft);
  border: 1px solid var(--about-accent-border);
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 600;
  color: var(--about-accent);
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 800px;
}

.stat-item {
  border-radius: 20px;
  border: 1px solid var(--about-line);
  background: var(--about-card);
  padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #111827;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--about-subtle);
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card {
  background: var(--about-card);
  border: 1px solid var(--about-line);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 2px 4px -2px rgba(0, 0, 0, 0.03);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -4px rgba(0, 0, 0, 0.06), 0 8px 12px -6px rgba(0, 0, 0, 0.04);
}

.card-wide {
  margin-bottom: 24px;
}

.card-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: #4b5563;
  background: #f3f4f6;
  margin-bottom: 16px;
}

.card-title {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 12px;
  color: var(--about-text);
  letter-spacing: -0.01em;
}

.card-desc {
  margin: 0 0 20px;
  color: var(--about-subtle);
  line-height: 1.6;
  font-size: 15px;
}

.position-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.position-item {
  border: 1px solid var(--about-line);
  border-radius: 16px;
  padding: 20px;
  background: #fafafa;
}

.position-item h4 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.position-item p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.pipeline {
  display: grid;
  gap: 12px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--about-line);
  border-radius: 16px;
  background: #fafafa;
  transition: background-color 0.2s;
}

.step-item:hover {
  background: #f9fafb;
}

.step-no {
  min-width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
  color: var(--about-accent);
  background: var(--about-accent-soft);
}

.step-main h4 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.step-main p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.tech-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.tech-group {
  flex: 1;
}

.group-title {
  margin: 0 0 16px;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.5px;
  color: #9ca3af;
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
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  color: #4b5563;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.5;
}

.list li:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.list li:first-child {
  padding-top: 0;
}

.list li::before {
  content: "";
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3b82f6;
}

.logic-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.logic-list li {
  padding: 16px;
  border-radius: 12px;
  background: #fafafa;
  border: 1px solid #f3f4f6;
  color: #4b5563;
  line-height: 1.6;
  font-size: 14px;
  font-weight: 500;
}

.key {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 6px;
  margin-right: 12px;
  font-size: 13px;
  font-weight: 700;
  color: var(--about-accent);
  background: var(--about-accent-soft);
}

.roadmap {
  display: grid;
  gap: 20px;
}

.roadmap-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-top: 6px;
  background: linear-gradient(135deg, #0ea5e9, #4f46e5);
  box-shadow: 0 0 0 4px var(--about-accent-soft);
}

.roadmap-item h4 {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.roadmap-item p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

/* 动画效果 */
.fade-in { animation: fadeIn 0.6s ease-out forwards; }
.slide-up { opacity: 0; animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards; animation-delay: var(--delay, 0s); }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 992px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .position-grid {
    grid-template-columns: 1fr;
  }

  .tech-list {
    grid-template-columns: 1fr;
  }

  .stat-row {
    grid-template-columns: 1fr;
    max-width: none;
  }

  .title {
    font-size: 32px;
  }

  .card {
    padding: 24px;
  }
}

@media (max-width: 640px) {
  .title {
    font-size: 28px;
    letter-spacing: -0.02em;
  }

  .subtitle {
    font-size: 15px;
  }

  .step-item {
    padding: 16px;
    gap: 12px;
  }

  .step-no {
    min-width: 32px;
    height: 32px;
    font-size: 12px;
  }
  
  .about-page {
    padding: 16px;
  }
}
</style>

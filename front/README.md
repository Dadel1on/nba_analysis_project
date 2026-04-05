# NBA 球员数据分析与赛季表现预测系统（前端）

本项目是系统前端实现，技术栈为 Vue3 + TypeScript + Vite + Element Plus + ECharts。

当前版本已完成完整演示链路：
- 欢迎页、仪表盘、球员分析、球队对比、预测中心、数据管理
- 球员单场预测、比赛胜负预测、赛季趋势预测、模型可解释性展示
- 球员详情页钻取

## 技术栈

- Vue 3 + Vue Router
- TypeScript
- Element Plus
- ECharts
- Axios
- TailwindCSS

## 快速开始

```bash
npm install
npm run dev
```

其他命令：

```bash
npm run check
npm run lint
npm run build
```

## 环境变量

复制 `.env.example` 为 `.env.local` 并按需修改：

```bash
VITE_API_BASE_URL=
VITE_API_PROXY_TARGET=http://127.0.0.1:8080
```

说明：
- `VITE_API_BASE_URL`：Axios `baseURL`，通常保持空字符串并使用 Vite 代理。
- `VITE_API_PROXY_TARGET`：Vite 开发代理目标，默认 `http://127.0.0.1:8080`。

## 页面与能力

- `/dashboard`：统计概览、趋势图、位置分布、近期比赛、球员榜单
- `/players`：球员搜索、列表展示、详情跳转
- `/players/:id`：球员详情
- `/teams`：双队选择对比、赛季与维度切换、指标图表
- `/prediction`：
  - 球员单场预测
  - 比赛胜负预测（球队下拉选择）
  - 赛季趋势预测（折线图）
  - 模型可解释性（关键因子）
- `/admin`：CSV 上传与上传历史记录

## 前后端接口契约（当前前端使用）

### 查询接口

- `GET /api/players?name=&page=&limit=`
- `GET /api/players/:id`
- `GET /api/teams`
- `GET /api/dashboard/overview`
- `GET /api/admin/upload/history`

### 预测接口

- `POST /api/prediction/player`
  - 请求体：`{ "player_id": number }`
- `POST /api/prediction/match`
  - 请求体：`{ "home_team": string, "away_team": string }`
- `POST /api/prediction/season`
  - 请求体：`{ "player_id": number }`
- `GET /api/prediction/explain/player?player_id=`

### 对比与管理接口

- `POST /api/teams/compare`
  - 请求体：`{ "teams": [string, string], "season"?: number, "metric"?: "all" | "offense" | "defense" }`
- `POST /api/admin/upload`（由上传组件直接调用）

## 兼容与回退策略

为保证答辩演示稳定性，前端在部分后端接口未完成时提供回退机制：
- 仪表盘数据回退
- 球队对比结果回退
- 赛季趋势与可解释性回退
- 上传历史回退

后端接口就绪后，页面会自动优先显示真实数据。

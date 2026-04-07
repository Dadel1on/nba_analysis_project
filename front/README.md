# NBA 球员数据分析与赛季表现预测系统（前端）

这是项目的 Vue 3 前端实现，负责页面展示、数据联调、图表绘制和部分接口回退处理。

## 技术栈

- Vue 3 + Vue Router
- TypeScript
- Vite
- Element Plus
- ECharts
- Axios
- TailwindCSS

## 快速开始

在 [front](.) 目录执行：

```bash
npm install
npm run dev
```

常用命令：

```bash
npm run check
npm run lint
npm run build
```

## 环境变量

复制 `.env.example` 为 `.env.local` 后按需修改：

```bash
VITE_API_BASE_URL=
VITE_API_PROXY_TARGET=http://127.0.0.1:8080
```

说明：

- `VITE_API_BASE_URL` 通常保持为空，前端通过代理访问后端。
- `VITE_API_PROXY_TARGET` 用于开发环境代理，默认指向本地后端。

## 页面与能力

- `/dashboard`：统计概览、趋势图、位置分布、近期比赛、球员榜单
- `/players`：球员搜索、列表展示、详情跳转
- `/players/:id`：球员详情
- `/teams`：双队选择对比、赛季与维度切换、指标图表
- `/prediction`：球员单场预测、比赛胜负预测、赛季趋势预测、模型可解释性
- `/admin`：CSV 上传与上传历史记录

## 前后端接口契约

完整接口定义请查看 [docs/API_CONTRACT.md](docs/API_CONTRACT.md)。

当前前端默认使用的接口包括：

- `GET /api/dashboard/overview`
- `GET /api/players?name=&page=&limit=`
- `GET /api/players/:id`
- `GET /api/teams`
- `POST /api/teams/compare`
- `POST /api/prediction/player`
- `POST /api/prediction/match`
- `POST /api/prediction/season`
- `GET /api/prediction/explain/player?player_id=`
- `POST /api/admin/upload`
- `GET /api/admin/upload/history`

## 兼容与回退策略

为保证演示稳定性，前端在后端能力尚未完全就绪时提供回退机制：

- 仪表盘数据回退
- 球队对比结果回退
- 赛季趋势与可解释性回退
- 上传历史回退

后端接口就绪后，页面会自动优先显示真实数据。

## 目录提示

- `src/api`：接口封装层
- `src/pages`：页面入口
- `src/views`：主要业务视图
- `src/router`：路由配置
- `src/composables`：可复用逻辑
- `src/components`：通用组件

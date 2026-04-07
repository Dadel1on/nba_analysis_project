# API 接口协议（前端 <-> Spring Boot）

本文件定义当前前端实现依赖的接口协议，供后端联调使用。

相关文档：

- [文档索引](../../docs/%E6%96%87%E6%A1%A3%E7%B4%A2%E5%BC%95.md)
- [前端交付检查清单](DELIVERY_CHECKLIST.md)

## 通用响应格式

统一返回格式：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

说明：

- `code=0` 表示成功。
- `message` 用于展示提示或错误说明。
- `data` 承载业务数据，部分接口也兼容直接返回对象。

## 1. 仪表盘

### GET /api/dashboard/overview

用途：仪表盘总览。

返回数据结构：

```ts
interface DashboardPayload {
  stats: {
    activePlayers: number
    teams: number
    seasonGames: number
    predictions: number
  }
  trend: Array<{
    season: string
    avgPoints: number
  }>
  positionDistribution: Array<{
    name: string
    value: number
  }>
  recentGames: Array<{
    date: string
    homeTeam: string
    awayTeam: string
    score: string
  }>
  topPlayers: Array<{
    name: string
    team: string
    points: number
  }>
}
```

常见字段说明：

- `stats.activePlayers`：活跃球员数
- `stats.teams`：球队数，当前期望为 30
- `stats.seasonGames`：赛季比赛总数
- `stats.predictions`：预测记录数
- `trend`：赛季均值趋势
- `positionDistribution`：位置分布
- `recentGames`：近期比赛列表
- `topPlayers`：球员排行榜

### GET /api/dashboard/games

用途：分页获取近期比赛。

查询参数：

- `page?: number`，默认 `1`
- `limit?: number`，默认 `20`

返回数据结构：

```ts
interface PaginatedPayload<T> {
  items: T[]
  page: number
  limit: number
  total: number
}
```

单条比赛结构与仪表盘中的 `recentGames` 一致。

### GET /api/dashboard/rankings

用途：分页获取球员排行榜。

查询参数：

- `page?: number`，默认 `1`
- `limit?: number`，默认 `20`

单条排行榜结构与 `topPlayers` 一致。

## 2. 球员

### GET /api/players

查询参数：

- `name?: string`
- `page?: number`，默认 `1`
- `limit?: number`，默认 `20`

返回数据结构：

```ts
interface PlayerSummary {
  id: number
  name: string
  team: string | null
  position: string | null
  stats: {
    points: number
    rebounds: number
    assists: number
  }
}
```

说明：

- `team` 可能为空，表示当前没有可用球队名。
- `position` 可能为空或为 `UNK`，前端会展示为未知位置。

### GET /api/players/:id

用途：根据球员 ID 获取详情。

返回结构与 `PlayerSummary` 一致。

响应策略：

- 找不到球员时返回 HTTP `404`，body 为 `ApiResponse`，`message` 为 `Player not found`。

## 3. 球队

### GET /api/teams

用途：获取球队列表。

返回数据结构：

```ts
interface TeamSummary {
  id?: number
  name: string
  city: string
  conference: string
}
```

说明：

- `conference` 当前应返回 `East` 或 `West`。
- 如果后端数据完整，球队数量应为 30。

### POST /api/teams/compare

用途：对比两支球队的赛季表现。

请求体：

```ts
interface TeamCompareRequest {
  teams: [string, string]
  season?: number
  metric?: 'all' | 'offense' | 'defense'
}
```

返回数据结构：

```ts
interface TeamComparisonResult {
  teams: Array<{
    team: string
    points: number
    rebounds: number
    assists: number
    wins: number
    winRate: number
  }>
}
```

## 4. 预测

### POST /api/prediction/player

请求体：

```ts
interface PlayerPredictionRequest {
  player_id: number
}
```

返回数据结构：

```ts
interface PlayerPredictionResult {
  player_id: number
  predicted_stats: {
    points: number
    rebounds: number
    assists: number
  }
  confidence: number
}
```

### POST /api/prediction/match

请求体：

```ts
interface MatchPredictionRequest {
  home_team: string
  away_team: string
}
```

返回数据结构：

```ts
interface MatchPredictionResult {
  home_team: string
  away_team: string
  home_win_probability: number
  away_win_probability: number
  confidence: number
  key_factors?: string[]
}
```

### POST /api/prediction/season

请求体：

```ts
interface SeasonPredictionRequest {
  player_id: number
}
```

返回数据结构：

```ts
interface SeasonTrendPredictionResult {
  player_id: number
  history: Array<{
    season: string
    points: number
    rebounds: number
    assists: number
  }>
  forecast: Array<{
    season: string
    points: number
    rebounds: number
    assists: number
  }>
}
```

### GET /api/prediction/explain/player?player_id=1

返回数据结构：

```ts
interface PredictionExplainability {
  player_id: number
  model_name: string
  model_version: string
  key_factors: Array<{
    factor: string
    contribution: number
    direction: 'positive' | 'negative'
  }>
}
```

## 5. 管理

### POST /api/admin/upload

用途：上传 CSV 文件并记录导入历史。

请求：

- `Content-Type: multipart/form-data`
- 字段名：`file`

响应结构：

```ts
interface UploadHistoryItem {
  fileName: string
  rows: number
  status: 'success' | 'failed' | 'processing'
  createdAt: string
}
```

### GET /api/admin/upload/history

用途：获取上传历史记录。

### GET /api/admin/system/status

用途：查看当前数据源状态与基础统计。

### POST /api/admin/spark/run

用途：异步触发 Spark 作业。

### GET /api/admin/spark/history

用途：查看 Spark 作业历史。

### GET /api/admin/spark/predictions

用途：查看最近的 Spark 预测结果。

## 6. 错误约定

推荐后端统一使用 HTTP 状态码配合 `message`：

- `400`：参数错误
- `404`：资源不存在
- `500`：服务内部错误

前端在请求失败时展示 `message`，并保留兜底回退数据。

## 7. 与前端类型的对应关系

前端 `src/api/types.ts` 中的核心类型与本协议一一对应：

- `DashboardPayload`
- `PlayerSummary`
- `TeamSummary`
- `TeamComparisonResult`
- `PlayerPredictionResult`
- `MatchPredictionResult`
- `SeasonTrendPredictionResult`
- `PredictionExplainability`
- `UploadHistoryItem`
- `SparkJobRunItem`
- `PredictionResultItem`

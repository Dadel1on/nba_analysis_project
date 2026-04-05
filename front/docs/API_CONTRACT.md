# API 接口协议（前端 <-> Spring Boot）

本文件定义当前前端实现依赖的接口协议，供后端联调使用。

## 通用响应格式

推荐统一返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

前端对部分接口支持兼容模式：既支持 `data` 包裹，也支持直接返回业务对象。

## 1. 仪表盘

### GET /api/dashboard/overview

- 用途：仪表盘总览
- 兼容字段命名：camelCase / snake_case

示例（推荐）：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "stats": {
      "activePlayers": 450,
      "teams": 30,
      "seasonGames": 1230,
      "predictions": 1525
    },
    "trend": [
      { "season": "2021", "avgPoints": 110.6 },
      { "season": "2022", "avgPoints": 114.7 },
      { "season": "2023", "avgPoints": 115.4 }
    ],
    "positionDistribution": [
      { "name": "后卫 (G)", "value": 120 },
      { "name": "前锋 (F)", "value": 150 }
    ],
    "recentGames": [
      { "date": "2026-03-22", "homeTeam": "湖人", "awayTeam": "勇士", "score": "116-112" }
    ],
    "topPlayers": [
      { "name": "Player A", "team": "LAL", "points": 30.5 }
    ]
  }
}
```

## 2. 球员

### GET /api/players

查询参数：
- `name?: string`
- `page?: number`
- `limit?: number`

响应示例：

```json
{
  "data": {
    "players": [
      {
        "id": 1,
        "name": "LeBron James",
        "team": "Lakers",
        "position": "F",
        "stats": { "points": 27.1, "rebounds": 7.5, "assists": 7.4 }
      }
    ]
  }
}
```

### GET /api/players/:id

响应可返回单对象或 `players` 数组，前端均可兼容。

## 3. 球队

### GET /api/teams

响应示例：

```json
{
  "data": {
    "teams": [
      { "id": 1, "name": "Lakers", "city": "Los Angeles", "conference": "West" }
    ]
  }
}
```

### POST /api/teams/compare

请求体：

```json
{
  "teams": ["Lakers", "Warriors"],
  "season": 2025,
  "metric": "all"
}
```

`metric` 可选值：`all | offense | defense`

响应示例：

```json
{
  "data": {
    "teams": [
      { "team": "Lakers", "points": 114.2, "rebounds": 44.1, "assists": 26.3, "wins": 51, "winRate": 0.622 },
      { "team": "Warriors", "points": 116.8, "rebounds": 43.3, "assists": 29.1, "wins": 49, "winRate": 0.598 }
    ]
  }
}
```

## 4. 预测

### POST /api/prediction/player

请求体：

```json
{ "player_id": 1 }
```

响应示例：

```json
{
  "data": {
    "player_id": 1,
    "predicted_stats": { "points": 29.2, "rebounds": 8.1, "assists": 7.0 },
    "confidence": 0.87
  }
}
```

### POST /api/prediction/match

请求体：

```json
{ "home_team": "Lakers", "away_team": "Warriors" }
```

响应示例：

```json
{
  "data": {
    "home_team": "Lakers",
    "away_team": "Warriors",
    "home_win_probability": 0.56,
    "away_win_probability": 0.44,
    "confidence": 0.82,
    "key_factors": ["主场优势", "近期防守效率"]
  }
}
```

### POST /api/prediction/season

请求体：

```json
{ "player_id": 1 }
```

响应示例：

```json
{
  "data": {
    "player_id": 1,
    "history": [
      { "season": "2021", "points": 25.1, "rebounds": 7.2, "assists": 6.1 }
    ],
    "forecast": [
      { "season": "2024", "points": 28.2, "rebounds": 8.0, "assists": 7.0 }
    ]
  }
}
```

### GET /api/prediction/explain/player?player_id=1

响应示例：

```json
{
  "data": {
    "player_id": 1,
    "model_name": "Baseline-Regression-Model",
    "model_version": "v1.2.0",
    "key_factors": [
      { "factor": "近5场得分均值", "contribution": 0.32, "direction": "positive" },
      { "factor": "对手防守效率", "contribution": -0.14, "direction": "negative" }
    ]
  }
}
```

## 5. 管理

### POST /api/admin/upload

- Content-Type: `multipart/form-data`
- 字段：`file`

### GET /api/admin/upload/history

响应示例：

```json
{
  "data": [
    { "fileName": "players_2024.csv", "rows": 4280, "status": "success", "createdAt": "2026-03-24 09:10:43" }
  ]
}
```

## 错误约定

推荐后端统一使用 HTTP 状态码 + `message`：
- 400: 参数错误
- 404: 资源不存在
- 500: 服务内部错误

前端会展示 `message`，并在请求失败时提供兜底提示。

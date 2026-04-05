# backend-spring

用于 NBA 分析与预测系统的 Spring Boot 后端（纯前后端版本）。

## 运行要求

- JDK 17+
- Maven 3.9+
- MySQL 8+

## 启动方式

```powershell
cd backend-spring
./scripts/use-java17.ps1
./scripts/run-backend.ps1
```

默认端口：`8080`

## 数据库配置

当前使用 `application.yml` 中的固定 MySQL 配置：

- `jdbc:mysql://127.0.0.1:3306/nba_stats`
- `username: root`
- `password: 111111`

首次使用请先初始化库表：

```bash
mysql -u root -p111111 nba_stats < src/main/resources/schema.sql
mysql -u root -p111111 nba_stats < src/main/resources/data.sql
```

## 主要 API 路由

- `GET /api/dashboard/overview`
- `GET /api/players?name=&page=&limit=`
- `GET /api/players/{id}`
- `GET /api/teams`
- `POST /api/teams/compare`
- `POST /api/prediction/player`
- `POST /api/prediction/match`
- `POST /api/prediction/season`
- `GET /api/prediction/explain/player?player_id=`
- `POST /api/admin/upload`
- `GET /api/admin/upload/history`
- `GET /api/admin/system/status`

## 说明

- 响应封装格式：`{ code, message, data }`
- 当前后端专注常规业务接口，不包含离线作业调度与特征工程能力

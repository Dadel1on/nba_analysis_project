# backend-spring

这是 NBA 分析项目的 Spring Boot 后端，负责 REST API、JPA 持久化、上传管理和 Spark 任务编排。

## 运行要求

- JDK 17+
- Maven 3.9+
- MySQL 8+ 或 MariaDB 5.5+

## 启动方式

在 [backend-spring](.) 目录执行：

```powershell
mvn spring-boot:run
```

默认端口：`8080`

## 默认配置

后端默认读取 [application.yml](src/main/resources/application.yml)：

- `spring.datasource.url`：`jdbc:mysql://master:3306/nba_stats?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- `DB_USERNAME` 默认值：`root`
- `DB_PASSWORD` 默认值：`123456`

如果本地没有 `master` 这个主机名，请在启动前通过环境变量覆盖数据源地址、用户名和密码。

## 数据初始化

如果要重建基础库表，可执行：

```powershell
mysql -u root -p nba_stats < src/main/resources/schema.sql
mysql -u root -p nba_stats < src/main/resources/data.sql
```

## 维护脚本

- [scripts/sql/01_performance_indexes.sql](scripts/sql/01_performance_indexes.sql)：索引优化，适合重复执行
- [scripts/sql/02_backfill_advanced_metrics.sql](scripts/sql/02_backfill_advanced_metrics.sql)：回填高级指标
- [scripts/sql/03_dimension_enrichment.sql](scripts/sql/03_dimension_enrichment.sql)：补齐球队与球员维度字段
- [scripts/spark_nba_ml.py](scripts/spark_nba_ml.py)：Spark 预测脚本，训练随机森林并写回预测快照

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
- 当前后端专注在线 API 和基础数据维护，Spark 训练属于独立脚本，不会影响普通页面接口启动

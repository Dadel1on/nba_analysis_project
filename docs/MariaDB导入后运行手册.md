# NBA MariaDB 导入后运行手册

本文档用于说明 NBA 数据导入 MariaDB 之后需要执行的后续维护步骤，目标是完成接口烟雾测试、性能优化、指标回填和维度补齐，保证后端接口和前端展示都能稳定运行。

## 1. 适用场景

当你已经完成原始数据导入后，可以按本文档继续做以下工作：

1. 检查后端和前端联调链路是否正常
2. 执行高级指标回填
3. 执行查询性能优化
4. 执行维度补齐，减少接口空值

## 2. 接口烟雾测试

### 本地后端启动

在 `backend-spring` 目录下执行：

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/nba_stats?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<your_password>"
$env:APP_SPARK_ENABLED="false"
mvn spring-boot:run
```

如果你保留仓库默认配置，后端会指向 `master:3306`；上面的覆盖配置仅适合本地 MariaDB。

### 接口检查

后端启动后，在另一个终端执行：

```powershell
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/dashboard/overview" -UseBasicParsing
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/players?page=1&limit=3" -UseBasicParsing
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/teams" -UseBasicParsing
```

### 前端检查

在 `front` 目录执行：

```powershell
npm install
npm run build
npm run dev
```

默认代理目标是 `http://127.0.0.1:8080`。

## 3. 性能优化

执行：

- [01_performance_indexes.sql](../backend-spring/scripts/sql/01_performance_indexes.sql)

该脚本用于补齐索引，可以重复执行。

## 4. 高级指标回填

执行：

- [02_backfill_advanced_metrics.sql](../backend-spring/scripts/sql/02_backfill_advanced_metrics.sql)

回填策略：

1. 同球员同赛季均值填充空值
2. 联盟同赛季均值兜底
3. 回写 `fact_player_season.avg_ts_pct`

## 5. 维度补齐

执行：

- [03_dimension_enrichment.sql](../backend-spring/scripts/sql/03_dimension_enrichment.sql)

该脚本补齐：

1. `dim_team.conference` 和 `dim_team.division`
2. `dim_player.position` 的空值和变体值，统一收敛为 `G/F/C/G-F/F-C/UNK`

这样可以减少 `/api/teams` 和 `/api/players` 的空值展示。

## 6. 结果检查

```sql
SELECT 'dim_team' AS t, COUNT(*) AS c FROM nba_stats.dim_team
UNION ALL
SELECT 'dim_player', COUNT(*) FROM nba_stats.dim_player
UNION ALL
SELECT 'fact_game', COUNT(*) FROM nba_stats.fact_game
UNION ALL
SELECT 'fact_team_season', COUNT(*) FROM nba_stats.fact_team_season
UNION ALL
SELECT 'fact_player_game', COUNT(*) FROM nba_stats.fact_player_game
UNION ALL
SELECT 'fact_player_season', COUNT(*) FROM nba_stats.fact_player_season;

SELECT
  COUNT(*) AS total_rows,
  SUM(ts_pct IS NOT NULL) AS ts_rows,
  SUM(off_rating IS NOT NULL) AS off_rating_rows,
  ROUND(SUM(ts_pct IS NOT NULL) / COUNT(*) * 100, 2) AS ts_pct_fill_rate,
  ROUND(SUM(off_rating IS NOT NULL) / COUNT(*) * 100, 2) AS off_rating_fill_rate
FROM nba_stats.fact_player_game;
```

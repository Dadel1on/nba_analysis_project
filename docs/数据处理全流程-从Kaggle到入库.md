# NBA 数据处理全流程（从 Kaggle 到 MariaDB 入库）

## 1. 目标

本文档用于固化本项目的数据处理 SOP，覆盖以下完整链路：

1. 从 Kaggle 下载原始数据
2. 识别当前仓库保留的数据脚本与表结构
3. 将原始 CSV 导入 MariaDB
4. 执行索引优化、指标回填和维度补齐
5. 验证前后端接口展示是否符合预期
6. 常见问题与修复方案

适用环境：

- OS：Windows
- 数据库：MariaDB 5.5.68 或 MySQL 8+
- 工具：Navicat 16、MySQL 命令行或自定义导入脚本
- 项目根目录：D:/Software/nba_analysis_project

> 说明：当前工作区不再包含早期的 CSV 预处理脚本 `preprocess_for_navicat.py`，因此本文档会以“原始数据 + 数据库脚本 + Spark 脚本”的方式描述当前可复现流程。

---

## 2. 原始数据准备（Kaggle）

### 2.1 下载数据集

数据集地址：

- https://www.kaggle.com/datasets/eoinamoore/historical-nba-data-and-player-box-scores

从 Kaggle 下载 eoin-nba 数据集到本地目录：

- D:/Software/data/eoin-nba

目录下关键文件示例：

1. Players.csv
2. TeamHistories.csv
3. Games.csv
4. PlayerStatistics.csv
5. PlayerStatisticsAdvanced.csv
6. TeamStatistics.csv
7. TeamStatisticsAdvanced.csv

---

## 3. 清洗与预处理

### 3.1 当前仓库的可用脚本

当前仓库保留的脚本主要分为两类：

1. [backend-spring/scripts/sql/01_performance_indexes.sql](../backend-spring/scripts/sql/01_performance_indexes.sql)
2. [backend-spring/scripts/sql/02_backfill_advanced_metrics.sql](../backend-spring/scripts/sql/02_backfill_advanced_metrics.sql)
3. [backend-spring/scripts/sql/03_dimension_enrichment.sql](../backend-spring/scripts/sql/03_dimension_enrichment.sql)
4. [backend-spring/scripts/spark_nba_ml.py](../backend-spring/scripts/spark_nba_ml.py)

这意味着：当前仓库已经具备“导入后优化”和“离线预测”能力，但原始 CSV 清洗与拆表导出逻辑需要由外部脚本或手工 ETL 完成。

### 3.2 建议的导入方式

对于 Kaggle 原始 CSV，建议采用以下顺序：

1. 先导入 `Players.csv`、`TeamHistories.csv`、`Games.csv`、`PlayerStatistics.csv` 等原始数据到临时表或目标表
2. 再执行 [01_performance_indexes.sql](../backend-spring/scripts/sql/01_performance_indexes.sql) 提升查询性能
3. 接着执行 [02_backfill_advanced_metrics.sql](../backend-spring/scripts/sql/02_backfill_advanced_metrics.sql) 回填缺失高级指标
4. 最后执行 [03_dimension_enrichment.sql](../backend-spring/scripts/sql/03_dimension_enrichment.sql) 补齐维度字段

如果你使用 Navicat 导入，可以直接从原始 CSV 生成表数据；如果你使用命令行或 ETL 工具，则优先保证字段类型与本文第 5 节一致。

---

## 4. 历史分区/分表产物说明

这部分描述的是项目早期清洗流程中常见的导出结果，当前仓库不再包含对应的自动生成脚本；如果你有外部 ETL 或手工清洗流程，建议仍按这个结构组织中间产物：

1. dim_team.csv
2. dim_player.csv
3. fact_game.csv
4. fact_player_game.csv
5. fact_player_season.csv
6. fact_team_season.csv
7. prediction_snapshot.csv
8. spark_job_run.csv
9. upload_history.csv
10. _summary.csv（文件行数统计）

说明：

- 第 7~9 为业务表模板（通常初始为空）
- 如果你通过外部 ETL 生成这些文件，导入顺序仍建议先维表，再事实表，再业务表

---

## 5. 数据库表详细说明（作用 + 字段解释）

### 5.1 dim_team（球队维度表）

作用：存放球队主数据，是比赛表和球队赛季表的基础维度。

字段解释：

1. team_id：球队唯一 ID（主键，来源于联盟官方 ID）
2. abbreviation：球队缩写（如 LAL、BOS）
3. team_name：球队名称
4. city：球队所在城市
5. conference：分区（East/West）
6. division：赛区（如 Pacific、Atlantic）
7. is_active：是否活跃（1 活跃，0 非活跃）

### 5.2 dim_player（球员维度表）

作用：存放球员主数据，供球员比赛事实表、球员赛季事实表、预测结果表关联。

字段解释：

1. player_id：球员唯一 ID（主键）
2. full_name：球员全名
3. first_name：名
4. last_name：姓
5. birth_date：出生日期
6. height_inches：身高（英寸）
7. weight_lbs：体重（磅）
8. position：位置（G/F/C 及组合）
9. country：国籍
10. draft_year：选秀年份
11. draft_round：选秀轮次
12. draft_number：选秀顺位
13. is_active：是否活跃

### 5.3 fact_game（比赛事实表）

作用：存放一场比赛的主信息，是球员比赛明细 fact_player_game 的父表。

字段解释：

1. game_id：比赛唯一 ID（主键）
2. game_date：比赛日期
3. season_year：赛季年份标签（如 2024、2025）
4. home_team_id：主队 ID（外键 -> dim_team.team_id）
5. away_team_id：客队 ID（外键 -> dim_team.team_id）
6. home_points：主队得分
7. away_points：客队得分
8. game_status：比赛类型/状态（如 Regular Season）
9. attendance：上座人数
10. arena_name：场馆名称

### 5.4 fact_player_game（球员-比赛事实表）

作用：存放球员单场明细数据，是业务分析最细粒度事实表之一。

字段解释：

1. id：自增主键
2. game_id：比赛 ID（外键 -> fact_game.game_id）
3. player_id：球员 ID（外键 -> dim_player.player_id）
4. team_id：球员当场所属球队 ID（外键 -> dim_team.team_id）
5. minutes_played：出场时间（分钟）
6. points：得分
7. rebounds：篮板
8. assists：助攻
9. steals：抢断
10. blocks：盖帽
11. turnovers：失误
12. fouls：犯规
13. plus_minus：正负值
14. ts_pct：真实命中率
15. efg_pct：有效命中率
16. usg_pct：使用率
17. off_rating：进攻效率
18. def_rating：防守效率
19. net_rating：净效率
20. ast_pct：助攻率
21. reb_pct：篮板率

### 5.5 fact_player_season（球员-赛季事实表）

作用：按球员+赛季聚合，支撑趋势分析、球员预测和可解释性。

字段解释：

1. id：自增主键
2. player_id：球员 ID（外键）
3. season_year：赛季年份
4. games_played：出场场次
5. games_started：首发场次
6. avg_minutes：场均时间
7. avg_points：场均得分
8. avg_rebounds：场均篮板
9. avg_assists：场均助攻
10. avg_ts_pct：场均真实命中率
11. per：球员效率值（可空）
12. win_shares：胜利贡献值（可空）
13. source_tag：数据来源标签（如 kaggle_eoin_nba）
14. updated_at：最后更新时间

### 5.6 fact_team_season（球队-赛季事实表）

作用：按球队+赛季聚合，支撑球队对比和比赛预测中的球队状态特征。

字段解释：

1. id：自增主键
2. team_id：球队 ID（外键）
3. season_year：赛季年份
4. wins：胜场
5. losses：负场
6. off_rating：进攻效率
7. def_rating：防守效率
8. net_rating：净效率
9. pace：比赛节奏
10. updated_at：最后更新时间

### 5.7 prediction_snapshot（预测快照表）

作用：存储模型预测结果，方便回看历史预测输出。

字段解释：

1. id：自增主键
2. player_id：球员 ID（外键）
3. predicted_points：预测得分
4. predicted_rebounds：预测篮板
5. predicted_assists：预测助攻
6. confidence：置信度
7. model_version：模型版本
8. created_at：预测生成时间

### 5.8 spark_job_run（Spark 作业记录表）

作用：记录离线作业运行历史，用于任务追踪与运维排障。

字段解释：

1. id：自增主键
2. job_name：任务名
3. status：运行状态（running/success/failed/skipped）
4. created_at：创建时间
5. updated_at：最后更新时间
6. detail：日志或错误详情

### 5.9 upload_history（上传历史表）

作用：记录后台数据上传行为，支撑管理端审计和数据流追踪。

字段解释：

1. id：自增主键
2. file_name：上传文件名
3. rows_count：上传行数
4. status：上传状态
5. created_at：上传时间

---

## 6. 初始化数据库（无种子数据，生产推荐）

当前仓库的 `schema.sql` 和 `data.sql` 更适合作为开发环境初始化基线；如果你已经有完整的 Kaggle 原始数据，推荐以原始导入为主，再执行后续 SQL 脚本补齐指标和维度。

当前仓库可直接执行或复用的内容是：

1. [backend-spring/src/main/resources/schema.sql](../backend-spring/src/main/resources/schema.sql)
2. [backend-spring/src/main/resources/data.sql](../backend-spring/src/main/resources/data.sql)
3. [backend-spring/scripts/sql/01_performance_indexes.sql](../backend-spring/scripts/sql/01_performance_indexes.sql)
4. [backend-spring/scripts/sql/02_backfill_advanced_metrics.sql](../backend-spring/scripts/sql/02_backfill_advanced_metrics.sql)
5. [backend-spring/scripts/sql/03_dimension_enrichment.sql](../backend-spring/scripts/sql/03_dimension_enrichment.sql)
6. [backend-spring/scripts/spark_nba_ml.py](../backend-spring/scripts/spark_nba_ml.py)

初始化脚本：

- [backend-spring/database/mysql_init_no_seed.sql](../backend-spring/database/mysql_init_no_seed.sql)

执行方式：

1. 在 Navicat 打开 SQL 文件
2. 执行完成后得到 9 张核心表和索引/外键

如需带种子数据版本：

- [backend-spring/database/mysql_init.sql](../backend-spring/database/mysql_init.sql)

---

## 7. Navicat 导入流程

### 6.1 推荐导入顺序

1. dim_team.csv
2. dim_player.csv
3. fact_game.csv
4. fact_player_game.csv
5. fact_player_season.csv
6. fact_team_season.csv
7. prediction_snapshot.csv
8. spark_job_run.csv
9. upload_history.csv

### 6.2 通用导入参数模板

1. 编码：UTF-8
2. 记录分隔符：CRLF
3. 字段分隔符：逗号 (,)
4. 文本识别符：双引号 (")
5. 字段名行：1
6. 第一个数据行：2
7. 日期排序：YMD
8. 日期分隔符：-（必须是横杠）
9. 时间分隔符：:
10. 小数点符号：.
11. 导入模式：追加（或先清空后追加）

### 6.3 主键/自增处理规则

1. dim_team.team_id、dim_player.player_id、fact_game.game_id：从 CSV 映射导入
2. 自增主键 id（如 fact_player_game.id）：不要手动映射 CSV id
3. 若 CSV 无 id 列（当前即如此），由数据库自动生成

---

## 8. 导入后一键验收

执行项目验收 SQL（建议保留在 Navicat 查询收藏）：

1. 行数阈值检查
2. 重复键检查
3. 外键完整性检查
4. 关键字段非空检查
5. 赛季范围检查

验收通过标准：

- pass_count = total_checks
- fail_count = 0
- overall_status = PASS

### 8.1 一键验收 SQL（可直接在 Navicat 执行）

```sql
USE nba_stats;

SELECT *
FROM (
    SELECT '01_dim_team_count' AS check_item,
      CONCAT(COUNT(*)) AS actual,
      '>= 30' AS expected,
      IF(COUNT(*) >= 30, 'PASS', 'FAIL') AS status
    FROM dim_team

    UNION ALL
    SELECT '02_dim_player_count',
      CONCAT(COUNT(*)),
      '>= 1000',
      IF(COUNT(*) >= 1000, 'PASS', 'FAIL')
    FROM dim_player

    UNION ALL
    SELECT '03_fact_game_count',
      CONCAT(COUNT(*)),
      '>= 1000',
      IF(COUNT(*) >= 1000, 'PASS', 'FAIL')
    FROM fact_game

    UNION ALL
    SELECT '04_fact_player_game_count',
      CONCAT(COUNT(*)),
      '>= 100000',
      IF(COUNT(*) >= 100000, 'PASS', 'FAIL')
    FROM fact_player_game

    UNION ALL
    SELECT '05_fact_player_season_count',
      CONCAT(COUNT(*)),
      '>= 1000',
      IF(COUNT(*) >= 1000, 'PASS', 'FAIL')
    FROM fact_player_season

    UNION ALL
    SELECT '06_fact_team_season_count',
      CONCAT(COUNT(*)),
      '>= 100',
      IF(COUNT(*) >= 100, 'PASS', 'FAIL')
    FROM fact_team_season

    UNION ALL
    SELECT '07_dup_fact_player_game(game_id,player_id)',
      CONCAT(IFNULL(SUM(c - 1), 0)),
      '= 0',
      IF(IFNULL(SUM(c - 1), 0) = 0, 'PASS', 'FAIL')
    FROM (
   SELECT COUNT(*) AS c
   FROM fact_player_game
   GROUP BY game_id, player_id
   HAVING COUNT(*) > 1
    ) t

    UNION ALL
    SELECT '08_dup_fact_player_season(player_id,season_year)',
      CONCAT(IFNULL(SUM(c - 1), 0)),
      '= 0',
      IF(IFNULL(SUM(c - 1), 0) = 0, 'PASS', 'FAIL')
    FROM (
   SELECT COUNT(*) AS c
   FROM fact_player_season
   GROUP BY player_id, season_year
   HAVING COUNT(*) > 1
    ) t

    UNION ALL
    SELECT '09_dup_fact_team_season(team_id,season_year)',
      CONCAT(IFNULL(SUM(c - 1), 0)),
      '= 0',
      IF(IFNULL(SUM(c - 1), 0) = 0, 'PASS', 'FAIL')
    FROM (
   SELECT COUNT(*) AS c
   FROM fact_team_season
   GROUP BY team_id, season_year
   HAVING COUNT(*) > 1
    ) t

    UNION ALL
    SELECT '10_orphan_pg_player',
      CONCAT(COUNT(*)),
      '= 0',
      IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_game pg
    LEFT JOIN dim_player p ON pg.player_id = p.player_id
    WHERE p.player_id IS NULL

    UNION ALL
    SELECT '11_orphan_pg_team',
      CONCAT(COUNT(*)),
      '= 0',
      IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_game pg
    LEFT JOIN dim_team t ON pg.team_id = t.team_id
    WHERE t.team_id IS NULL

    UNION ALL
    SELECT '12_orphan_pg_game',
      CONCAT(COUNT(*)),
      '= 0',
      IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_game pg
    LEFT JOIN fact_game g ON pg.game_id = g.game_id
    WHERE g.game_id IS NULL

    UNION ALL
    SELECT '13_orphan_ps_player',
      CONCAT(COUNT(*)),
      '= 0',
      IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_season ps
    LEFT JOIN dim_player p ON ps.player_id = p.player_id
    WHERE p.player_id IS NULL

    UNION ALL
    SELECT '14_orphan_ts_team',
      CONCAT(COUNT(*)),
      '= 0',
      IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_team_season ts
    LEFT JOIN dim_team t ON ts.team_id = t.team_id
    WHERE t.team_id IS NULL

    UNION ALL
    SELECT '15_fact_game_season_range',
      CONCAT(IFNULL(MIN(season_year), 0), '~', IFNULL(MAX(season_year), 0)),
      'min>=2014 AND max<=2100',
      IF(IFNULL(MIN(season_year), 0) >= 2014 AND IFNULL(MAX(season_year), 0) <= 2100, 'PASS', 'FAIL')
    FROM fact_game

    UNION ALL
    SELECT '16_player_season_has_2023',
      CONCAT(SUM(CASE WHEN season_year = 2023 THEN 1 ELSE 0 END)),
      '> 0',
      IF(SUM(CASE WHEN season_year = 2023 THEN 1 ELSE 0 END) > 0, 'PASS', 'FAIL')
    FROM fact_player_season

    UNION ALL
    SELECT '17_game_not_null_key_fields',
      CONCAT(COUNT(*)),
      '= 0',
      IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_game
    WHERE game_date IS NULL
  OR season_year IS NULL
  OR home_team_id IS NULL
  OR away_team_id IS NULL
) qa
ORDER BY check_item;

SELECT
    SUM(CASE WHEN status = 'PASS' THEN 1 ELSE 0 END) AS pass_count,
    SUM(CASE WHEN status = 'FAIL' THEN 1 ELSE 0 END) AS fail_count,
    COUNT(*) AS total_checks,
    IF(SUM(CASE WHEN status = 'FAIL' THEN 1 ELSE 0 END) = 0, 'PASS', 'FAIL') AS overall_status
FROM (
    SELECT IF(COUNT(*) >= 30, 'PASS', 'FAIL') AS status FROM dim_team
    UNION ALL SELECT IF(COUNT(*) >= 1000, 'PASS', 'FAIL') FROM dim_player
    UNION ALL SELECT IF(COUNT(*) >= 1000, 'PASS', 'FAIL') FROM fact_game
    UNION ALL SELECT IF(COUNT(*) >= 100000, 'PASS', 'FAIL') FROM fact_player_game
    UNION ALL SELECT IF(COUNT(*) >= 1000, 'PASS', 'FAIL') FROM fact_player_season
    UNION ALL SELECT IF(COUNT(*) >= 100, 'PASS', 'FAIL') FROM fact_team_season

    UNION ALL
    SELECT IF(IFNULL(SUM(c - 1), 0) = 0, 'PASS', 'FAIL')
    FROM (SELECT COUNT(*) AS c FROM fact_player_game GROUP BY game_id, player_id HAVING COUNT(*) > 1) t

    UNION ALL
    SELECT IF(IFNULL(SUM(c - 1), 0) = 0, 'PASS', 'FAIL')
    FROM (SELECT COUNT(*) AS c FROM fact_player_season GROUP BY player_id, season_year HAVING COUNT(*) > 1) t

    UNION ALL
    SELECT IF(IFNULL(SUM(c - 1), 0) = 0, 'PASS', 'FAIL')
    FROM (SELECT COUNT(*) AS c FROM fact_team_season GROUP BY team_id, season_year HAVING COUNT(*) > 1) t

    UNION ALL
    SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_game pg LEFT JOIN dim_player p ON pg.player_id = p.player_id
    WHERE p.player_id IS NULL

    UNION ALL
    SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_game pg LEFT JOIN dim_team t ON pg.team_id = t.team_id
    WHERE t.team_id IS NULL

    UNION ALL
    SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_game pg LEFT JOIN fact_game g ON pg.game_id = g.game_id
    WHERE g.game_id IS NULL

    UNION ALL
    SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_player_season ps LEFT JOIN dim_player p ON ps.player_id = p.player_id
    WHERE p.player_id IS NULL

    UNION ALL
    SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_team_season ts LEFT JOIN dim_team t ON ts.team_id = t.team_id
    WHERE t.team_id IS NULL

    UNION ALL
    SELECT IF(IFNULL(MIN(season_year), 0) >= 2014 AND IFNULL(MAX(season_year), 0) <= 2100, 'PASS', 'FAIL')
    FROM fact_game

    UNION ALL
    SELECT IF(SUM(CASE WHEN season_year = 2023 THEN 1 ELSE 0 END) > 0, 'PASS', 'FAIL')
    FROM fact_player_season

    UNION ALL
    SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL')
    FROM fact_game
    WHERE game_date IS NULL
  OR season_year IS NULL
  OR home_team_id IS NULL
  OR away_team_id IS NULL
) final_qa;
```

---

## 9. 常见问题与修复

### 9.1 问题：fact_game 的 game_date 导入后全部变成 0000-00-00

现象：

- 验收项 game_not_null_key_fields 失败
- zero_game_date = fact_game 总行数

根因：

- Navicat 导入参数中“日期分隔符”错误设置成 /（应为 -）

修复步骤：

1. 建中间表 staging_fact_game_fix（仅 game_id, game_date, season_year）
2. 用正确日期参数重新导入 fact_game.csv 到 staging
3. 执行回填更新：

```sql
UPDATE fact_game fg
JOIN staging_fact_game_fix s ON s.game_id = fg.game_id
SET fg.game_date = s.game_date,
    fg.season_year = s.season_year;
```

4. 验证：

```sql
SELECT
  COUNT(*) AS total_rows,
  SUM(CASE WHEN game_date IS NULL THEN 1 ELSE 0 END) AS null_game_date,
  SUM(CASE WHEN game_date = '0000-00-00' THEN 1 ELSE 0 END) AS zero_game_date
FROM fact_game;
```

期望：

- null_game_date = 0
- zero_game_date = 0

---

## 10. 当前已验证结果（本次实施）

本次流程已完成并通过验收，最终状态：

1. 17/17 检查项全部 PASS
2. overall_status = PASS
3. 外键完整性与关键字段完整性均通过

### 10.1 实际验收明细（实测）

| check_item | actual | expected | status |
|---|---:|---|---|
| 01_dim_team_count | 47 | >= 30 | PASS |
| 02_dim_player_count | 6688 | >= 1000 | PASS |
| 03_fact_game_count | 17574 | >= 1000 | PASS |
| 04_fact_player_game_count | 462959 | >= 100000 | PASS |
| 05_fact_player_season_count | 8225 | >= 1000 | PASS |
| 06_fact_team_season_count | 390 | >= 100 | PASS |
| 07_dup_fact_player_game(game_id,player_id) | 0 | = 0 | PASS |
| 08_dup_fact_player_season(player_id,season_year) | 0 | = 0 | PASS |
| 09_dup_fact_team_season(team_id,season_year) | 0 | = 0 | PASS |
| 10_orphan_pg_player | 0 | = 0 | PASS |
| 11_orphan_pg_team | 0 | = 0 | PASS |
| 12_orphan_pg_game | 0 | = 0 | PASS |
| 13_orphan_ps_player | 0 | = 0 | PASS |
| 14_orphan_ts_team | 0 | = 0 | PASS |
| 15_fact_game_season_range | 2014~2026 | min>=2014 AND max<=2100 | PASS |
| 16_player_season_has_2023 | 638 | > 0 | PASS |
| 17_game_not_null_key_fields | 0 | = 0 | PASS |

查询耗时：17 rows in set (1.08 sec)

### 10.2 实际验收汇总（实测）

| pass_count | fail_count | total_checks | overall_status |
|---:|---:|---:|---|
| 17 | 0 | 17 | PASS |

查询耗时：1 row in set (1.12 sec)

---

## 11. 建议的日常操作规范

1. 每次重导前先执行无种子初始化脚本
2. 每次导入后必跑一键验收 SQL
3. 保留 _summary.csv 与验收结果截图作为导入凭证
4. 生产环境禁止手工改主事实表，优先走清洗脚本重产物

---

## 12. 原始字段到目标表字段映射总表

说明：以下映射保留了历史清洗脚本的字段约定，用于说明每张表的数据来源与转换逻辑；当前工作区不再包含对应的自动预处理脚本，因此这里只作为复现参考。

### 12.1 dim_team 映射

原始来源：TeamHistories.csv（主）+ TeamStatistics.csv（补）+ Games.csv（补）

| 目标字段 | 原始字段 | 说明 |
|---|---|---|
| team_id | teamId / hometeamId / awayteamId | 球队唯一 ID，去重后保留 |
| abbreviation | teamAbbrev | 主来源；缺失时为空字符串 |
| team_name | teamName / hometeamName / awayteamName | 球队名称 |
| city | teamCity / hometeamCity / awayteamCity | 球队城市 |
| conference | 推断字段 | 根据球队名称推断 East/West |
| division | 常量空值 | 当前阶段不细分赛区 |
| is_active | 常量 1 | 标记为活跃 |

### 12.2 dim_player 映射

原始来源：Players.csv

| 目标字段 | 原始字段 | 说明 |
|---|---|---|
| player_id | personId | 球员唯一 ID |
| full_name | firstName + lastName | 清洗后拼接 |
| first_name | firstName | 名 |
| last_name | lastName | 姓 |
| birth_date | birthDate | 转换为 DATE |
| height_inches | heightInches | 身高（英寸） |
| weight_lbs | bodyWeightLbs | 体重（磅） |
| position | guard/forward/center | 由三个布尔字段推断 G/F/C 或组合 |
| country | country | 国籍 |
| draft_year | draftYear | 选秀年份 |
| draft_round | draftRound | 选秀轮次 |
| draft_number | draftNumber | 选秀顺位 |
| is_active | 常量 1 | 标记为活跃 |

### 12.3 fact_game 映射

原始来源：Games.csv

| 目标字段 | 原始字段 | 说明 |
|---|---|---|
| game_id | gameId | 比赛 ID |
| game_date | gameDateTimeEst | 取日期部分 yyyy-MM-dd |
| season_year | gameDateTimeEst | 规则：10-12 月归到下一赛季 |
| home_team_id | hometeamId | 主队 ID |
| away_team_id | awayteamId | 客队 ID |
| home_points | homeScore | 主队得分 |
| away_points | awayScore | 客队得分 |
| game_status | gameType | 例如 Regular Season |
| attendance | attendance | 上座人数 |
| arena_name | arenaName | 场馆名称 |

### 12.4 fact_player_game 映射

原始来源：PlayerStatistics.csv（主）+ PlayerStatisticsAdvanced.csv（高级指标）+ TeamStatistics.csv（补 team_id）

| 目标字段 | 原始字段 | 说明 |
|---|---|---|
| game_id | PlayerStatistics.gameId | 比赛 ID |
| player_id | PlayerStatistics.personId | 球员 ID |
| team_id | TeamStatistics.teamId | 通过 game_id + team_name + team_city 关联回填 |
| minutes_played | numMinutes | 出场时间 |
| points | points | 得分 |
| rebounds | reboundsTotal | 篮板 |
| assists | assists | 助攻 |
| steals | steals | 抢断 |
| blocks | blocks | 盖帽 |
| turnovers | turnovers | 失误 |
| fouls | foulsPersonal | 犯规 |
| plus_minus | plusMinusPoints | 正负值 |
| ts_pct | PlayerStatisticsAdvanced.tsPct | 真实命中率 |
| efg_pct | efgPct | 有效命中率 |
| usg_pct | usgPct | 使用率 |
| off_rating | offRating | 进攻效率 |
| def_rating | defRating | 防守效率 |
| net_rating | netRating | 净效率 |
| ast_pct | astPct | 助攻率 |
| reb_pct | rebPct | 篮板率 |

### 12.5 fact_player_season 映射

原始来源：PlayerStatistics.csv（主聚合）+ PlayerStatisticsAdvanced.csv（avg_ts_pct）

| 目标字段 | 原始字段 | 说明 |
|---|---|---|
| player_id | personId | 球员 ID |
| season_year | gameDateTimeEst | 按赛季规则转换 |
| games_played | gameId | 去重计数 |
| games_started | 常量 0 | 当前数据未拆分首发 |
| avg_minutes | numMinutes | 均值聚合 |
| avg_points | points | 均值聚合 |
| avg_rebounds | reboundsTotal | 均值聚合 |
| avg_assists | assists | 均值聚合 |
| avg_ts_pct | PlayerStatisticsAdvanced.tsPct | 均值聚合 |
| per | 空值 | 暂未从原始源计算 |
| win_shares | 空值 | 暂未从原始源计算 |
| source_tag | 常量 kaggle_eoin_nba | 数据标签 |
| updated_at | 当前时间 | 导出时间戳 |

### 12.6 fact_team_season 映射

原始来源：TeamStatistics.csv（胜负）+ TeamStatisticsAdvanced.csv（效率指标）

| 目标字段 | 原始字段 | 说明 |
|---|---|---|
| team_id | teamId | 球队 ID |
| season_year | gameDateTimeEst | 按赛季规则转换 |
| wins | win | 按赛季求和 |
| losses | gameId - wins | 按赛季计算 |
| off_rating | TeamStatisticsAdvanced.offRating | 均值聚合 |
| def_rating | defRating | 均值聚合 |
| net_rating | netRating | 均值聚合 |
| pace | pace | 均值聚合 |
| updated_at | 当前时间 | 导出时间戳 |

### 12.7 prediction_snapshot / spark_job_run / upload_history

原始来源：非 Kaggle 原始文件，属于业务运行期数据。

| 表名 | 初始化导出内容 | 说明 |
|---|---|---|
| prediction_snapshot | 空模板 CSV | 由预测接口或离线任务运行后写入 |
| spark_job_run | 空模板 CSV | 由 Spark 任务执行后写入 |
| upload_history | 空模板 CSV | 由后台上传接口写入 |

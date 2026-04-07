# NBA 分析项目

这是一个面向 NBA 数据分析与预测的前后端分离项目，包含 Vue 前端、Spring Boot 后端、MySQL/MariaDB 数据层，以及用于离线预测的 Spark 脚本和 SQL 维护脚本。

## 仓库结构

- [front](front)：Vue 3 + Vite 前端，负责页面展示、图表和接口联调
- [backend-spring](backend-spring)：Spring Boot 后端，负责 REST API、JPA 持久化和 Spark 任务编排
- [docs](docs)：集群启动、数据处理和交付说明

## 关键入口

- [backend-spring/src/main/resources/application.yml](backend-spring/src/main/resources/application.yml)：后端默认配置
- [backend-spring/scripts/spark_nba_ml.py](backend-spring/scripts/spark_nba_ml.py)：Spark 预测脚本
- [backend-spring/scripts/sql/01_performance_indexes.sql](backend-spring/scripts/sql/01_performance_indexes.sql)：索引优化
- [backend-spring/scripts/sql/02_backfill_advanced_metrics.sql](backend-spring/scripts/sql/02_backfill_advanced_metrics.sql)：高级指标回填
- [backend-spring/scripts/sql/03_dimension_enrichment.sql](backend-spring/scripts/sql/03_dimension_enrichment.sql)：维度补齐
- [front/docs/API_CONTRACT.md](front/docs/API_CONTRACT.md)：前后端接口协议
- [docs/项目结构与运行指南.md](docs/%E9%A1%B9%E7%9B%AE%E7%BB%93%E6%9E%84%E4%B8%8E%E8%BF%90%E8%A1%8C%E6%8C%87%E5%8D%97.md)：结构与运行总览
- [docs/文档索引.md](docs/%E6%96%87%E6%A1%A3%E7%B4%A2%E5%BC%95.md)：文档总入口

## 运行前提

- Windows 10/11
- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8+ 或 MariaDB 5.5+
- 如果要跑 Spark 预测，还需要 HDFS、Spark 集群和 MySQL JDBC 驱动

## 后端启动

进入 [backend-spring](backend-spring) 后执行：

```powershell
mvn spring-boot:run
```

后端默认读取 [application.yml](backend-spring/src/main/resources/application.yml)，当前默认数据源指向 `jdbc:mysql://master:3306/nba_stats`，账号密码由环境变量 `DB_USERNAME` 和 `DB_PASSWORD` 覆盖，默认值分别是 `root` 和 `123456`。

如果需要切换到本地数据库，可在启动前设置环境变量覆盖 `spring.datasource.url`、`DB_USERNAME` 和 `DB_PASSWORD`。

## 前端启动

进入 [front](front) 后执行：

```powershell
npm install
npm run dev
```

前端开发服务器会通过 Vite 代理把 `/api` 请求转发到后端。

## 数据库初始化与维护

如果需要重建 schema 或补充数据，优先查看这些脚本：

1. [schema.sql](backend-spring/src/main/resources/schema.sql)
2. [data.sql](backend-spring/src/main/resources/data.sql)
3. [01_performance_indexes.sql](backend-spring/scripts/sql/01_performance_indexes.sql)
4. [02_backfill_advanced_metrics.sql](backend-spring/scripts/sql/02_backfill_advanced_metrics.sql)
5. [03_dimension_enrichment.sql](backend-spring/scripts/sql/03_dimension_enrichment.sql)

## Spark 预测任务

Spark 任务由 [backend-spring/scripts/spark_nba_ml.py](backend-spring/scripts/spark_nba_ml.py) 提供，脚本从 HDFS 读取球员比赛数据，训练随机森林模型，并把预测结果写回 `prediction_snapshot` 表。

该脚本依赖：

- HDFS 上的 `PlayerStatistics.csv`
- Spark 集群可用
- JDBC 驱动 `mysql-connector-j`
- 目标数据库可通过 `master:3306` 访问

## 文档导航

- [项目结构与运行指南](docs/%E9%A1%B9%E7%9B%AE%E7%BB%93%E6%9E%84%E4%B8%8E%E8%BF%90%E8%A1%8C%E6%8C%87%E5%8D%97.md)
- [启动说明（集群环境）](docs/%E5%90%AF%E5%8A%A8%E8%AF%B4%E6%98%8E-%E9%9B%86%E7%BE%A4%E7%8E%AF%E5%A2%83.md)
- [数据处理全流程](docs/%E6%95%B0%E6%8D%AE%E5%A4%84%E7%90%86%E5%85%A8%E6%B5%81%E7%A8%8B-%E4%BB%8EKaggle%E5%88%B0%E5%85%A5%E5%BA%93.md)
- [MariaDB 导入后运行手册](docs/MariaDB%E5%AF%BC%E5%85%A5%E5%90%8E%E8%BF%90%E8%A1%8C%E6%89%8B%E5%86%8C.md)
- [文档索引](docs/%E6%96%87%E6%A1%A3%E7%B4%A2%E5%BC%95.md)
- [前端 API 协议](front/docs/API_CONTRACT.md)
- [前端交付清单](front/docs/DELIVERY_CHECKLIST.md)

## 快速验证

1. 启动后端后访问 `http://127.0.0.1:8080/api/dashboard/overview`，确认返回 `code/message/data`。
2. 启动前端后打开开发地址，确认首页和路由能正常加载。
3. 进入“球员分析”“球队对比”“预测中心”“数据管理”逐项检查核心页面。

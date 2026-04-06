# Wyatt SQLite -> MariaDB Post-Import Runbook

This runbook covers the 3 follow-up tasks after loading data:

1. API/frontend integration smoke checks
2. Advanced metric backfill
3. Performance index optimization
4. Dimension enrichment for API completeness

## 1) Integration Smoke Checks

### Backend startup (local MariaDB)

Run in PowerShell from `backend-spring`:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/nba_stats?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<your_password>"
$env:APP_SPARK_ENABLED="false"
mvn spring-boot:run
```

If startup works, check API endpoints in another shell:

```powershell
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/dashboard/overview" -UseBasicParsing
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/players?page=1&limit=3" -UseBasicParsing
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/teams" -UseBasicParsing
```

### Frontend check

Run in `front`:

```powershell
npm install
npm run build
npm run dev
```

Default proxy target is `http://127.0.0.1:8080` via `vite.config.ts`.

## 2) Performance Index Optimization

Execute:

- `backend-spring/scripts/sql/01_performance_indexes.sql`

This script safely adds missing indexes and can be re-run.

## 3) Advanced Metrics Backfill

Execute:

- `backend-spring/scripts/sql/02_backfill_advanced_metrics.sql`

Backfill strategy:

1. Same player+season averages fill null advanced metrics.
2. League season averages fill remaining nulls.
3. Refresh `fact_player_season.avg_ts_pct` from updated game facts.

## 4) Dimension Enrichment (Recommended)

Execute:

- `backend-spring/scripts/sql/03_dimension_enrichment.sql`

This step fills:

1. `dim_team.conference` and `dim_team.division` for all 30 teams
2. `dim_player.position` null/variant values into a compact set (`G/F/C/G-F/F-C/UNK`)

It helps avoid null fields in `/api/teams` and `/api/players` responses.

## 4) Post-Check Queries

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

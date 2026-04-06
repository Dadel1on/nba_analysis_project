USE nba_stats;

-- Goal: increase advanced-metric availability for prediction features.
-- Strategy:
-- 1) fill nulls from same player + season averages (high confidence)
-- 2) fill remaining nulls from league season averages (low confidence fallback)

DROP TEMPORARY TABLE IF EXISTS tmp_player_season_adv;
CREATE TEMPORARY TABLE tmp_player_season_adv AS
SELECT
  pg.player_id,
  g.season_year,
  AVG(pg.ts_pct) AS ts_pct,
  AVG(pg.efg_pct) AS efg_pct,
  AVG(pg.usg_pct) AS usg_pct,
  AVG(pg.off_rating) AS off_rating,
  AVG(pg.def_rating) AS def_rating,
  AVG(pg.net_rating) AS net_rating,
  AVG(pg.ast_pct) AS ast_pct,
  AVG(pg.reb_pct) AS reb_pct
FROM fact_player_game pg
JOIN fact_game g ON g.game_id = pg.game_id
GROUP BY pg.player_id, g.season_year;

DROP TEMPORARY TABLE IF EXISTS tmp_league_season_adv;
CREATE TEMPORARY TABLE tmp_league_season_adv AS
SELECT
  g.season_year,
  AVG(pg.ts_pct) AS ts_pct,
  AVG(pg.efg_pct) AS efg_pct,
  AVG(pg.usg_pct) AS usg_pct,
  AVG(pg.off_rating) AS off_rating,
  AVG(pg.def_rating) AS def_rating,
  AVG(pg.net_rating) AS net_rating,
  AVG(pg.ast_pct) AS ast_pct,
  AVG(pg.reb_pct) AS reb_pct
FROM fact_player_game pg
JOIN fact_game g ON g.game_id = pg.game_id
GROUP BY g.season_year;

-- Pass 1: same player-season fill
UPDATE fact_player_game pg
JOIN fact_game g ON g.game_id = pg.game_id
JOIN tmp_player_season_adv s
  ON s.player_id = pg.player_id
 AND s.season_year = g.season_year
SET
  pg.ts_pct = COALESCE(pg.ts_pct, s.ts_pct),
  pg.efg_pct = COALESCE(pg.efg_pct, s.efg_pct),
  pg.usg_pct = COALESCE(pg.usg_pct, s.usg_pct),
  pg.off_rating = COALESCE(pg.off_rating, s.off_rating),
  pg.def_rating = COALESCE(pg.def_rating, s.def_rating),
  pg.net_rating = COALESCE(pg.net_rating, s.net_rating),
  pg.ast_pct = COALESCE(pg.ast_pct, s.ast_pct),
  pg.reb_pct = COALESCE(pg.reb_pct, s.reb_pct)
WHERE
  pg.ts_pct IS NULL OR pg.efg_pct IS NULL OR pg.usg_pct IS NULL OR
  pg.off_rating IS NULL OR pg.def_rating IS NULL OR pg.net_rating IS NULL OR
  pg.ast_pct IS NULL OR pg.reb_pct IS NULL;

-- Pass 2: league-season fallback fill
UPDATE fact_player_game pg
JOIN fact_game g ON g.game_id = pg.game_id
JOIN tmp_league_season_adv l ON l.season_year = g.season_year
SET
  pg.ts_pct = COALESCE(pg.ts_pct, l.ts_pct),
  pg.efg_pct = COALESCE(pg.efg_pct, l.efg_pct),
  pg.usg_pct = COALESCE(pg.usg_pct, l.usg_pct),
  pg.off_rating = COALESCE(pg.off_rating, l.off_rating),
  pg.def_rating = COALESCE(pg.def_rating, l.def_rating),
  pg.net_rating = COALESCE(pg.net_rating, l.net_rating),
  pg.ast_pct = COALESCE(pg.ast_pct, l.ast_pct),
  pg.reb_pct = COALESCE(pg.reb_pct, l.reb_pct)
WHERE
  pg.ts_pct IS NULL OR pg.efg_pct IS NULL OR pg.usg_pct IS NULL OR
  pg.off_rating IS NULL OR pg.def_rating IS NULL OR pg.net_rating IS NULL OR
  pg.ast_pct IS NULL OR pg.reb_pct IS NULL;

-- Refresh season aggregate ts_pct after backfill
UPDATE fact_player_season fps
JOIN (
  SELECT
    pg.player_id,
    g.season_year,
    AVG(pg.ts_pct) AS avg_ts_pct
  FROM fact_player_game pg
  JOIN fact_game g ON g.game_id = pg.game_id
  GROUP BY pg.player_id, g.season_year
) s
  ON s.player_id = fps.player_id
 AND s.season_year = fps.season_year
SET
  fps.avg_ts_pct = s.avg_ts_pct,
  fps.source_tag = 'wyatt_sqlite_advfill',
  fps.updated_at = NOW();

-- Post-check: fill rates after backfill
SELECT
  COUNT(*) AS total_rows,
  SUM(ts_pct IS NOT NULL) AS ts_rows,
  SUM(off_rating IS NOT NULL) AS off_rating_rows,
  ROUND(SUM(ts_pct IS NOT NULL) / COUNT(*) * 100, 2) AS ts_pct_fill_rate,
  ROUND(SUM(off_rating IS NOT NULL) / COUNT(*) * 100, 2) AS off_rating_fill_rate
FROM fact_player_game;

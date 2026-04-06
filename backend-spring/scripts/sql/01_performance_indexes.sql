USE nba_stats;

-- Create missing indexes without failing on re-runs.
DELIMITER $$
DROP PROCEDURE IF EXISTS ensure_index $$
CREATE PROCEDURE ensure_index(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_sql TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table
      AND index_name = p_index
  ) THEN
    SET @stmt = p_sql;
    PREPARE s FROM @stmt;
    EXECUTE s;
    DEALLOCATE PREPARE s;
  END IF;
END $$
DELIMITER ;

-- fact_game: dashboard, recent games, team form
CALL ensure_index(
  'fact_game',
  'idx_fact_game_date_id',
  'ALTER TABLE fact_game ADD INDEX idx_fact_game_date_id (game_date, game_id)'
);

CALL ensure_index(
  'fact_game',
  'idx_fact_game_season_date_id',
  'ALTER TABLE fact_game ADD INDEX idx_fact_game_season_date_id (season_year, game_date, game_id)'
);

CALL ensure_index(
  'fact_game',
  'idx_fact_game_home_date',
  'ALTER TABLE fact_game ADD INDEX idx_fact_game_home_date (home_team_id, game_date)'
);

CALL ensure_index(
  'fact_game',
  'idx_fact_game_away_date',
  'ALTER TABLE fact_game ADD INDEX idx_fact_game_away_date (away_team_id, game_date)'
);

-- fact_player_game: player timeline and latest-team lookup
CALL ensure_index(
  'fact_player_game',
  'idx_fpg_player_game_id',
  'ALTER TABLE fact_player_game ADD INDEX idx_fpg_player_game_id (player_id, game_id, id)'
);

CALL ensure_index(
  'fact_player_game',
  'idx_fpg_team_game',
  'ALTER TABLE fact_player_game ADD INDEX idx_fpg_team_game (team_id, game_id)'
);

-- fact_player_season: latest season ranking and player trend query
CALL ensure_index(
  'fact_player_season',
  'idx_fps_season_points_player',
  'ALTER TABLE fact_player_season ADD INDEX idx_fps_season_points_player (season_year, avg_points, player_id)'
);

CALL ensure_index(
  'fact_player_season',
  'idx_fps_player_latest_cover',
  'ALTER TABLE fact_player_season ADD INDEX idx_fps_player_latest_cover (player_id, season_year, avg_points, avg_rebounds, avg_assists)'
);

-- fact_team_season: season leaderboard
CALL ensure_index(
  'fact_team_season',
  'idx_fts_season_wins_team',
  'ALTER TABLE fact_team_season ADD INDEX idx_fts_season_wins_team (season_year, wins, team_id)'
);

-- prediction snapshot: latest prediction per player
SET @pred_created_type := (
  SELECT LOWER(data_type)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'prediction_snapshot'
    AND column_name = 'created_at'
  LIMIT 1
);

SET @pred_sql := IFNULL(
  IF(
    @pred_created_type IN ('tinytext', 'text', 'mediumtext', 'longtext', 'tinyblob', 'blob', 'mediumblob', 'longblob'),
    'ALTER TABLE prediction_snapshot ADD INDEX idx_pred_player_created (player_id, created_at(32))',
    'ALTER TABLE prediction_snapshot ADD INDEX idx_pred_player_created (player_id, created_at)'
  ),
  'SELECT 1'
);

CALL ensure_index('prediction_snapshot', 'idx_pred_player_created', @pred_sql);

DROP PROCEDURE IF EXISTS ensure_index;

-- Quick index audit
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN (
    'idx_fact_game_date_id',
    'idx_fact_game_season_date_id',
    'idx_fact_game_home_date',
    'idx_fact_game_away_date',
    'idx_fpg_player_game_id',
    'idx_fpg_team_game',
    'idx_fps_season_points_player',
    'idx_fps_player_latest_cover',
    'idx_fts_season_wins_team',
    'idx_pred_player_created'
  )
GROUP BY table_name, index_name
ORDER BY table_name, index_name;

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS nba_stats
  DEFAULT CHARACTER SET utf8
  COLLATE utf8_general_ci;

USE nba_stats;

DROP TABLE IF EXISTS prediction_snapshot;
DROP TABLE IF EXISTS upload_history;
DROP TABLE IF EXISTS spark_job_run;
DROP TABLE IF EXISTS fact_player_game;
DROP TABLE IF EXISTS fact_player_season;
DROP TABLE IF EXISTS fact_team_season;
DROP TABLE IF EXISTS fact_game;
DROP TABLE IF EXISTS dim_player;
DROP TABLE IF EXISTS dim_team;

CREATE TABLE dim_team (
  team_id BIGINT NOT NULL,
  abbreviation VARCHAR(10) NOT NULL,
  team_name VARCHAR(80) NOT NULL,
  city VARCHAR(80) NULL,
  conference VARCHAR(20) NULL,
  division VARCHAR(30) NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (team_id),
  UNIQUE KEY uq_team_abbreviation (abbreviation),
  KEY idx_team_name (team_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE dim_player (
  player_id BIGINT NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  first_name VARCHAR(60) NULL,
  last_name VARCHAR(60) NULL,
  birth_date DATE NULL,
  height_inches INT NULL,
  weight_lbs INT NULL,
  position VARCHAR(20) NULL,
  country VARCHAR(50) NULL,
  draft_year INT NULL,
  draft_round INT NULL,
  draft_number INT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (player_id),
  KEY idx_player_name (full_name),
  KEY idx_player_position (position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE fact_game (
  game_id BIGINT NOT NULL,
  game_date DATE NOT NULL,
  season_year INT NOT NULL,
  home_team_id BIGINT NOT NULL,
  away_team_id BIGINT NOT NULL,
  home_points INT NOT NULL DEFAULT 0,
  away_points INT NOT NULL DEFAULT 0,
  game_status VARCHAR(40) NULL,
  attendance INT NULL,
  arena_name VARCHAR(100) NULL,
  PRIMARY KEY (game_id),
  KEY idx_game_date (game_date),
  KEY idx_game_season (season_year),
  KEY idx_game_home_date (home_team_id, game_date),
  KEY idx_game_away_date (away_team_id, game_date),
  CONSTRAINT fk_game_home_team FOREIGN KEY (home_team_id) REFERENCES dim_team(team_id),
  CONSTRAINT fk_game_away_team FOREIGN KEY (away_team_id) REFERENCES dim_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE fact_player_game (
  id BIGINT NOT NULL AUTO_INCREMENT,
  game_id BIGINT NOT NULL,
  player_id BIGINT NOT NULL,
  team_id BIGINT NOT NULL,
  minutes_played DOUBLE NULL,
  points INT NOT NULL DEFAULT 0,
  rebounds INT NOT NULL DEFAULT 0,
  assists INT NOT NULL DEFAULT 0,
  steals INT NOT NULL DEFAULT 0,
  blocks INT NOT NULL DEFAULT 0,
  turnovers INT NOT NULL DEFAULT 0,
  fouls INT NOT NULL DEFAULT 0,
  plus_minus INT NOT NULL DEFAULT 0,
  ts_pct DOUBLE NULL,
  efg_pct DOUBLE NULL,
  usg_pct DOUBLE NULL,
  off_rating DOUBLE NULL,
  def_rating DOUBLE NULL,
  net_rating DOUBLE NULL,
  ast_pct DOUBLE NULL,
  reb_pct DOUBLE NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_player_game (game_id, player_id),
  KEY idx_pg_player_id_desc (player_id, id),
  KEY idx_pg_game (game_id),
  KEY idx_pg_team (team_id),
  CONSTRAINT fk_pg_game FOREIGN KEY (game_id) REFERENCES fact_game(game_id),
  CONSTRAINT fk_pg_player FOREIGN KEY (player_id) REFERENCES dim_player(player_id),
  CONSTRAINT fk_pg_team FOREIGN KEY (team_id) REFERENCES dim_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE fact_player_season (
  id BIGINT NOT NULL AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  season_year INT NOT NULL,
  games_played INT NOT NULL DEFAULT 0,
  games_started INT NOT NULL DEFAULT 0,
  avg_minutes DOUBLE NULL,
  avg_points DOUBLE NULL,
  avg_rebounds DOUBLE NULL,
  avg_assists DOUBLE NULL,
  avg_ts_pct DOUBLE NULL,
  per DOUBLE NULL,
  win_shares DOUBLE NULL,
  source_tag VARCHAR(40) NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_player_season (player_id, season_year),
  KEY idx_ps_player_season (player_id, season_year),
  KEY idx_ps_season (season_year),
  CONSTRAINT fk_ps_player FOREIGN KEY (player_id) REFERENCES dim_player(player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE fact_team_season (
  id BIGINT NOT NULL AUTO_INCREMENT,
  team_id BIGINT NOT NULL,
  season_year INT NOT NULL,
  wins INT NOT NULL DEFAULT 0,
  losses INT NOT NULL DEFAULT 0,
  off_rating DOUBLE NULL,
  def_rating DOUBLE NULL,
  net_rating DOUBLE NULL,
  pace DOUBLE NULL,
  updated_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_team_season (team_id, season_year),
  KEY idx_ts_team_season (team_id, season_year),
  KEY idx_ts_season (season_year),
  CONSTRAINT fk_ts_team FOREIGN KEY (team_id) REFERENCES dim_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE prediction_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  predicted_points DOUBLE NOT NULL,
  predicted_rebounds DOUBLE NOT NULL,
  predicted_assists DOUBLE NOT NULL,
  confidence DOUBLE NOT NULL,
  model_version VARCHAR(60) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_pred_player_created (player_id, created_at),
  CONSTRAINT fk_pred_player FOREIGN KEY (player_id) REFERENCES dim_player(player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE upload_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  file_name VARCHAR(150) NOT NULL,
  rows_count INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_upload_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE spark_job_run (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  detail LONGTEXT NULL,
  PRIMARY KEY (id),
  KEY idx_spark_job_name (job_name),
  KEY idx_spark_job_status (status),
  KEY idx_spark_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;

-- NBA 统计分析系统数据库架构 V2 (星型模型设计)
-- 适配 Kaggle NBA Dataset (1947 - Present)

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 清理旧表
DROP TABLE IF EXISTS prediction_snapshot;
DROP TABLE IF EXISTS upload_history;
DROP TABLE IF EXISTS spark_job_run;
DROP TABLE IF EXISTS fact_player_game;
DROP TABLE IF EXISTS fact_player_season;
DROP TABLE IF EXISTS fact_team_season;
DROP TABLE IF EXISTS fact_game;
DROP TABLE IF EXISTS dim_player;
DROP TABLE IF EXISTS dim_team;

SET FOREIGN_KEY_CHECKS = 1;

-- 2. 维度表设计 (Dimension Tables)

-- 球队维度表
CREATE TABLE dim_team (
    team_id BIGINT PRIMARY KEY,
    abbreviation VARCHAR(10) NOT NULL,
    team_name VARCHAR(80) NOT NULL,
    city VARCHAR(80),
    conference VARCHAR(20),
    division VARCHAR(30),
    is_active BOOLEAN DEFAULT TRUE,
    KEY idx_team_abbrev (abbreviation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 球员维度表
CREATE TABLE dim_player (
    player_id BIGINT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    first_name VARCHAR(60),
    last_name VARCHAR(60),
    birth_date DATE,
    height_inches INT,
    weight_lbs INT,
    position VARCHAR(20),
    country VARCHAR(50),
    draft_year INT,
    draft_round INT,
    draft_number INT,
    is_active BOOLEAN DEFAULT TRUE,
    KEY idx_player_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 事实表设计 (Fact Tables)

-- 比赛事实表 (Game Metadata)
CREATE TABLE fact_game (
    game_id BIGINT PRIMARY KEY,
    game_date DATE NOT NULL,
    season_year INT NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    home_points INT DEFAULT 0,
    away_points INT DEFAULT 0,
    game_status VARCHAR(40),
    attendance INT,
    arena_name VARCHAR(100),
    KEY idx_game_date (game_date),
    KEY idx_game_season (season_year),
    CONSTRAINT fk_game_home_team FOREIGN KEY (home_team_id) REFERENCES dim_team(team_id),
    CONSTRAINT fk_game_away_team FOREIGN KEY (away_team_id) REFERENCES dim_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 球员-比赛详细事实表 (Player-Game Level Granularity)
-- 包含基础和进阶统计数据
CREATE TABLE fact_player_game (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    minutes_played DOUBLE,
    -- 基础数据
    points INT DEFAULT 0,
    rebounds INT DEFAULT 0,
    assists INT DEFAULT 0,
    steals INT DEFAULT 0,
    blocks INT DEFAULT 0,
    turnovers INT DEFAULT 0,
    fouls INT DEFAULT 0,
    plus_minus INT DEFAULT 0,
    -- 进阶数据 (从 Kaggle Advanced/Usage/Misc 获取)
    ts_pct DOUBLE,          -- 真实命中率 (True Shooting %)
    efg_pct DOUBLE,         -- 有效命中率 (Effective FG %)
    usg_pct DOUBLE,         -- 使用率 (Usage %)
    off_rating DOUBLE,      -- 进攻效率
    def_rating DOUBLE,      -- 防守效率
    net_rating DOUBLE,      -- 净效率
    ast_pct DOUBLE,         -- 助攻率
    reb_pct DOUBLE,         -- 篮板率
    KEY idx_p_g_player (player_id),
    KEY idx_p_g_game (game_id),
    CONSTRAINT fk_pg_game FOREIGN KEY (game_id) REFERENCES fact_game(game_id),
    CONSTRAINT fk_pg_player FOREIGN KEY (player_id) REFERENCES dim_player(player_id),
    CONSTRAINT fk_pg_team FOREIGN KEY (team_id) REFERENCES dim_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 球员-赛季聚合事实表
CREATE TABLE fact_player_season (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    season_year INT NOT NULL,
    games_played INT DEFAULT 0,
    games_started INT DEFAULT 0,
    avg_minutes DOUBLE,
    avg_points DOUBLE,
    avg_rebounds DOUBLE,
    avg_assists DOUBLE,
    avg_ts_pct DOUBLE,
    per DOUBLE,             -- 球员效率值 (Player Efficiency Rating)
    win_shares DOUBLE,      -- 胜利贡献值
    source_tag VARCHAR(40),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_player_season (player_id, season_year),
    CONSTRAINT fk_ps_player FOREIGN KEY (player_id) REFERENCES dim_player(player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 球队-赛季事实表
CREATE TABLE fact_team_season (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    season_year INT NOT NULL,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    off_rating DOUBLE,      -- 球队每百回合得分
    def_rating DOUBLE,      -- 球队每百回合失分
    net_rating DOUBLE,
    pace DOUBLE,            -- 比赛节奏
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_team_season (team_id, season_year),
    CONSTRAINT fk_ts_team FOREIGN KEY (team_id) REFERENCES dim_team(team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 业务与预测系统表 (保留并增强)

-- 预测结果快照
CREATE TABLE prediction_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    predicted_points DOUBLE NOT NULL,
    predicted_rebounds DOUBLE NOT NULL,
    predicted_assists DOUBLE NOT NULL,
    confidence DOUBLE NOT NULL,
    model_version VARCHAR(60) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_pred_player (player_id),
    CONSTRAINT fk_pred_player FOREIGN KEY (player_id) REFERENCES dim_player(player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 上传历史记录
CREATE TABLE upload_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(150) NOT NULL,
    rows_count INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Spark 离线作业执行记录
CREATE TABLE spark_job_run (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    run_key VARCHAR(80),
    spark_app_id VARCHAR(80),
    spark_ui_url VARCHAR(255),
    submit_host VARCHAR(120),
    started_at DATETIME,
    finished_at DATETIME,
    args_json JSON,
    conf_json JSON,
    input_ref JSON,
    output_ref JSON,
    row_counts_json JSON,
    error_stack LONGTEXT,
    detail LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_spark_job_name (job_name),
    KEY idx_spark_job_status (status),
    KEY idx_spark_job_app (spark_app_id),
    KEY idx_spark_job_run_key (run_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE spark_job_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_run_id BIGINT NOT NULL,
    step_name VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATETIME,
    finished_at DATETIME,
    detail LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_spark_step_run (job_run_id),
    KEY idx_spark_step_status (status),
    CONSTRAINT fk_spark_step_run FOREIGN KEY (job_run_id) REFERENCES spark_job_run(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE spark_job_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_run_id BIGINT NOT NULL,
    metric_key VARCHAR(120) NOT NULL,
    metric_value DOUBLE,
    metric_unit VARCHAR(30),
    tags_json JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_spark_metric_run (job_run_id),
    KEY idx_spark_metric_key (metric_key),
    CONSTRAINT fk_spark_metric_run FOREIGN KEY (job_run_id) REFERENCES spark_job_run(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE spark_job_artifact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_run_id BIGINT NOT NULL,
    artifact_type VARCHAR(40) NOT NULL,
    uri VARCHAR(500) NOT NULL,
    checksum VARCHAR(128),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_spark_artifact_run (job_run_id),
    KEY idx_spark_artifact_type (artifact_type),
    CONSTRAINT fk_spark_artifact_run FOREIGN KEY (job_run_id) REFERENCES spark_job_run(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

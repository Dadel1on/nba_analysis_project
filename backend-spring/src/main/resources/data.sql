-- 初始种子数据 V2
USE nba_stats;

-- 插入一些核心球队
INSERT IGNORE INTO dim_team (team_id, abbreviation, team_name, city, conference) VALUES
(1610612747, 'LAL', 'Lakers', 'Los Angeles', 'West'),
(1610612744, 'GSW', 'Warriors', 'San Francisco', 'West'),
(1610612738, 'BOS', 'Celtics', 'Boston', 'East'),
(1610612743, 'DEN', 'Nuggets', 'Denver', 'West'),
(1610612749, 'MIL', 'Bucks', 'Milwaukee', 'East'),
(1610612756, 'PHX', 'Suns', 'Phoenix', 'West'),
(1610612752, 'NYK', 'Knicks', 'New York', 'East'),
(1610612742, 'DAL', 'Mavericks', 'Dallas', 'West');

-- 插入一些核心球员
INSERT IGNORE INTO dim_player (player_id, full_name, position, country) VALUES
(2544, 'LeBron James', 'F', 'USA'),
(201939, 'Stephen Curry', 'G', 'USA'),
(203999, 'Nikola Jokic', 'C', 'Serbia'),
(1628369, 'Jayson Tatum', 'F', 'USA'),
(1629029, 'Luka Doncic', 'G', 'Slovenia'),
(203507, 'Giannis Antetokounmpo', 'F', 'Greece'),
(1626164, 'Devin Booker', 'G', 'USA'),
(1628973, 'Jalen Brunson', 'G', 'USA');

-- 为每个球员插入一些赛季平均数据
INSERT IGNORE INTO fact_player_season (player_id, season_year, games_played, avg_points, avg_rebounds, avg_assists, source_tag) VALUES
(2544, 2023, 71, 25.7, 7.3, 8.3, 'seed'),
(201939, 2023, 74, 26.4, 4.5, 5.1, 'seed'),
(203999, 2023, 79, 26.4, 12.4, 9.0, 'seed'),
(1629029, 2023, 70, 33.9, 9.2, 9.8, 'seed');

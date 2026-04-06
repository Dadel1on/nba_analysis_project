USE nba_stats;

-- Fill conference/division for all 30 NBA teams.
UPDATE dim_team
SET
  conference = CASE team_id
    WHEN 1610612737 THEN 'East'  -- ATL
    WHEN 1610612738 THEN 'East'  -- BOS
    WHEN 1610612739 THEN 'East'  -- CLE
    WHEN 1610612740 THEN 'West'  -- NOP
    WHEN 1610612741 THEN 'East'  -- CHI
    WHEN 1610612742 THEN 'West'  -- DAL
    WHEN 1610612743 THEN 'West'  -- DEN
    WHEN 1610612744 THEN 'West'  -- GSW
    WHEN 1610612745 THEN 'West'  -- HOU
    WHEN 1610612746 THEN 'West'  -- LAC
    WHEN 1610612747 THEN 'West'  -- LAL
    WHEN 1610612748 THEN 'East'  -- MIA
    WHEN 1610612749 THEN 'East'  -- MIL
    WHEN 1610612750 THEN 'West'  -- MIN
    WHEN 1610612751 THEN 'East'  -- BKN
    WHEN 1610612752 THEN 'East'  -- NYK
    WHEN 1610612753 THEN 'East'  -- ORL
    WHEN 1610612754 THEN 'East'  -- IND
    WHEN 1610612755 THEN 'East'  -- PHI
    WHEN 1610612756 THEN 'West'  -- PHX
    WHEN 1610612757 THEN 'West'  -- POR
    WHEN 1610612758 THEN 'West'  -- SAC
    WHEN 1610612759 THEN 'West'  -- SAS
    WHEN 1610612760 THEN 'West'  -- OKC
    WHEN 1610612761 THEN 'East'  -- TOR
    WHEN 1610612762 THEN 'West'  -- UTA
    WHEN 1610612763 THEN 'West'  -- MEM
    WHEN 1610612764 THEN 'East'  -- WAS
    WHEN 1610612765 THEN 'East'  -- DET
    WHEN 1610612766 THEN 'East'  -- CHA
    ELSE conference
  END,
  division = CASE team_id
    WHEN 1610612737 THEN 'Southeast'
    WHEN 1610612738 THEN 'Atlantic'
    WHEN 1610612739 THEN 'Central'
    WHEN 1610612740 THEN 'Southwest'
    WHEN 1610612741 THEN 'Central'
    WHEN 1610612742 THEN 'Southwest'
    WHEN 1610612743 THEN 'Northwest'
    WHEN 1610612744 THEN 'Pacific'
    WHEN 1610612745 THEN 'Southwest'
    WHEN 1610612746 THEN 'Pacific'
    WHEN 1610612747 THEN 'Pacific'
    WHEN 1610612748 THEN 'Southeast'
    WHEN 1610612749 THEN 'Central'
    WHEN 1610612750 THEN 'Northwest'
    WHEN 1610612751 THEN 'Atlantic'
    WHEN 1610612752 THEN 'Atlantic'
    WHEN 1610612753 THEN 'Southeast'
    WHEN 1610612754 THEN 'Central'
    WHEN 1610612755 THEN 'Atlantic'
    WHEN 1610612756 THEN 'Pacific'
    WHEN 1610612757 THEN 'Northwest'
    WHEN 1610612758 THEN 'Pacific'
    WHEN 1610612759 THEN 'Southwest'
    WHEN 1610612760 THEN 'Northwest'
    WHEN 1610612761 THEN 'Atlantic'
    WHEN 1610612762 THEN 'Northwest'
    WHEN 1610612763 THEN 'Southwest'
    WHEN 1610612764 THEN 'Southeast'
    WHEN 1610612765 THEN 'Central'
    WHEN 1610612766 THEN 'Southeast'
    ELSE division
  END;

-- Normalize player position values to a compact set.
UPDATE dim_player
SET position = CASE
  WHEN position IS NULL OR TRIM(position) = '' THEN 'UNK'
  WHEN UPPER(TRIM(position)) IN ('GUARD', 'PG', 'SG', 'G') THEN 'G'
  WHEN UPPER(TRIM(position)) IN ('FORWARD', 'SF', 'PF', 'F') THEN 'F'
  WHEN UPPER(TRIM(position)) IN ('CENTER', 'C') THEN 'C'
  WHEN UPPER(TRIM(position)) IN ('G-F', 'GF', 'F-G', 'FG') THEN 'G-F'
  WHEN UPPER(TRIM(position)) IN ('F-C', 'FC', 'C-F', 'CF') THEN 'F-C'
  ELSE position
END;

-- Verify enrichment result.
SELECT
  SUM(conference IS NULL OR conference = '') AS team_conference_nulls,
  SUM(division IS NULL OR division = '') AS team_division_nulls
FROM dim_team;

SELECT
  COUNT(*) AS total_players,
  SUM(position IS NULL OR TRIM(position) = '') AS position_nulls,
  SUM(position = 'UNK') AS position_unknowns,
  SUM(position = 'G') AS pos_g,
  SUM(position = 'F') AS pos_f,
  SUM(position = 'C') AS pos_c,
  SUM(position = 'G-F') AS pos_gf,
  SUM(position = 'F-C') AS pos_fc
FROM dim_player;

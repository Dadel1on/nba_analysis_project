from __future__ import annotations

from pathlib import Path
from datetime import datetime
import numpy as np
import pandas as pd


RAW_DIR = Path(r"D:/Software/data/eoin-nba")
OUT_DIR = Path(r"D:/Software/data/nba_cleaned_navicat")
MIN_SEASON_YEAR = 2014


def safe_bool_to_int(series: pd.Series, default: int = 0) -> pd.Series:
    mapped = (
        series.astype(str)
        .str.strip()
        .str.lower()
        .map({"1": 1, "0": 0, "true": 1, "false": 0, "w": 1, "l": 0, "win": 1, "loss": 0})
    )
    return mapped.fillna(default).astype(int)


def parse_dt(series: pd.Series) -> pd.Series:
    return pd.to_datetime(series, errors="coerce")


def build_dim_team(team_histories: pd.DataFrame, games: pd.DataFrame, team_stats: pd.DataFrame) -> pd.DataFrame:
    from_history = team_histories[["teamId", "teamAbbrev", "teamName", "teamCity"]].rename(
        columns={
            "teamId": "team_id",
            "teamAbbrev": "abbreviation",
            "teamName": "team_name",
            "teamCity": "city",
        }
    )

    # Backup from TeamStatistics in case some team IDs are missing in TeamHistories.
    from_team_stats = (
        team_stats[["teamId", "teamName", "teamCity"]]
        .drop_duplicates()
        .rename(columns={"teamId": "team_id", "teamName": "team_name", "teamCity": "city"})
    )
    from_team_stats["abbreviation"] = ""

    # Backup from Games for additional names.
    from_games_home = games[["hometeamId", "hometeamName", "hometeamCity"]].rename(
        columns={"hometeamId": "team_id", "hometeamName": "team_name", "hometeamCity": "city"}
    )
    from_games_away = games[["awayteamId", "awayteamName", "awayteamCity"]].rename(
        columns={"awayteamId": "team_id", "awayteamName": "team_name", "awayteamCity": "city"}
    )
    from_games = pd.concat([from_games_home, from_games_away], ignore_index=True).drop_duplicates()
    from_games["abbreviation"] = ""

    teams = pd.concat([from_history, from_team_stats, from_games], ignore_index=True)
    teams = teams.dropna(subset=["team_id"])
    teams["team_id"] = pd.to_numeric(teams["team_id"], errors="coerce").astype("Int64")
    teams = teams.dropna(subset=["team_id"]).copy()
    teams["team_id"] = teams["team_id"].astype("int64")
    teams["team_name"] = teams["team_name"].astype(str).str.strip()
    teams["city"] = teams["city"].astype(str).str.strip()

    teams = teams.sort_values(["team_id", "abbreviation"], ascending=[True, False])
    teams = teams.drop_duplicates(subset=["team_id"], keep="first")

    teams["conference"] = np.where(
        teams["team_name"].isin(
            {
                "Celtics",
                "Knicks",
                "Nets",
                "76ers",
                "Raptors",
                "Bulls",
                "Cavaliers",
                "Pistons",
                "Pacers",
                "Bucks",
                "Hawks",
                "Hornets",
                "Heat",
                "Magic",
                "Wizards",
            }
        ),
        "East",
        "West",
    )
    teams["division"] = ""
    teams["is_active"] = 1

    return teams[["team_id", "abbreviation", "team_name", "city", "conference", "division", "is_active"]]


def build_dim_player(players: pd.DataFrame) -> pd.DataFrame:
    out = players.rename(
        columns={
            "personId": "player_id",
            "firstName": "first_name",
            "lastName": "last_name",
            "birthDate": "birth_date",
            "bodyWeightLbs": "weight_lbs",
            "heightInches": "height_inches",
            "draftYear": "draft_year",
            "draftRound": "draft_round",
            "draftNumber": "draft_number",
            "country": "country",
        }
    ).copy()

    out["first_name"] = out["first_name"].fillna("").astype(str).str.strip()
    out["last_name"] = out["last_name"].fillna("").astype(str).str.strip()
    out["full_name"] = (out["first_name"] + " " + out["last_name"]).str.strip()

    g = safe_bool_to_int(out.get("guard", pd.Series([0] * len(out))))
    f = safe_bool_to_int(out.get("forward", pd.Series([0] * len(out))))
    c = safe_bool_to_int(out.get("center", pd.Series([0] * len(out))))

    out["position"] = np.select(
        [
            (g == 1) & (f == 1),
            (f == 1) & (c == 1),
            (g == 1) & (c == 1),
            g == 1,
            f == 1,
            c == 1,
        ],
        ["G-F", "F-C", "G-C", "G", "F", "C"],
        default="",
    )

    out["birth_date"] = parse_dt(out["birth_date"]).dt.date
    for col in ["height_inches", "weight_lbs", "draft_year", "draft_round", "draft_number"]:
        out[col] = pd.to_numeric(out[col], errors="coerce").astype("Int64")

    out["is_active"] = 1
    out["player_id"] = pd.to_numeric(out["player_id"], errors="coerce").astype("Int64")
    out = out.dropna(subset=["player_id"]).copy()
    out["player_id"] = out["player_id"].astype("int64")

    out = out[
        [
            "player_id",
            "full_name",
            "first_name",
            "last_name",
            "birth_date",
            "height_inches",
            "weight_lbs",
            "position",
            "country",
            "draft_year",
            "draft_round",
            "draft_number",
            "is_active",
        ]
    ].drop_duplicates(subset=["player_id"])

    return out


def build_fact_game(games: pd.DataFrame) -> pd.DataFrame:
    out = games.rename(
        columns={
            "gameId": "game_id",
            "gameDateTimeEst": "game_date_time",
            "hometeamId": "home_team_id",
            "awayteamId": "away_team_id",
            "homeScore": "home_points",
            "awayScore": "away_points",
            "gameType": "game_type",
            "arenaName": "arena_name",
        }
    ).copy()

    out["game_dt"] = parse_dt(out["game_date_time"])
    out["game_date"] = out["game_dt"].dt.date
    out["season_year"] = out["game_dt"].dt.year

    # NBA season usually spans two years; Oct-Dec belongs to next season label.
    month = out["game_dt"].dt.month
    out.loc[month >= 10, "season_year"] = out.loc[month >= 10, "season_year"] + 1

    out["game_status"] = out["game_type"].fillna("").astype(str)
    out["attendance"] = pd.to_numeric(out.get("attendance"), errors="coerce").astype("Int64")

    for col in ["game_id", "home_team_id", "away_team_id", "home_points", "away_points", "season_year"]:
        out[col] = pd.to_numeric(out[col], errors="coerce").astype("Int64")

    out = out.dropna(subset=["game_id", "home_team_id", "away_team_id", "game_date", "season_year"]).copy()
    out["game_id"] = out["game_id"].astype("int64")
    out["home_team_id"] = out["home_team_id"].astype("int64")
    out["away_team_id"] = out["away_team_id"].astype("int64")
    out["season_year"] = out["season_year"].astype("int64")
    out["home_points"] = out["home_points"].fillna(0).astype("int64")
    out["away_points"] = out["away_points"].fillna(0).astype("int64")

    out = out[
        [
            "game_id",
            "game_date",
            "season_year",
            "home_team_id",
            "away_team_id",
            "home_points",
            "away_points",
            "game_status",
            "attendance",
            "arena_name",
        ]
    ].drop_duplicates(subset=["game_id"])

    return out


def build_fact_player_game(player_stats: pd.DataFrame, player_adv: pd.DataFrame, team_stats: pd.DataFrame) -> pd.DataFrame:
    base = player_stats.rename(
        columns={
            "gameId": "game_id",
            "personId": "player_id",
            "numMinutes": "minutes_played",
            "plusMinusPoints": "plus_minus",
            "reboundsTotal": "rebounds",
            "foulsPersonal": "fouls",
            "playerteamName": "team_name",
            "playerteamCity": "team_city",
        }
    ).copy()

    base["game_id"] = pd.to_numeric(base["game_id"], errors="coerce").astype("Int64")
    base["player_id"] = pd.to_numeric(base["player_id"], errors="coerce").astype("Int64")

    # team_id from TeamStatistics by (game_id + team_name + team_city).
    team_map = team_stats[["gameId", "teamId", "teamName", "teamCity"]].rename(
        columns={"gameId": "game_id", "teamId": "team_id", "teamName": "team_name", "teamCity": "team_city"}
    )
    team_map["game_id"] = pd.to_numeric(team_map["game_id"], errors="coerce").astype("Int64")
    team_map["team_id"] = pd.to_numeric(team_map["team_id"], errors="coerce").astype("Int64")
    team_map["team_name"] = team_map["team_name"].astype(str).str.strip()
    team_map["team_city"] = team_map["team_city"].astype(str).str.strip()
    team_map = team_map.drop_duplicates(subset=["game_id", "team_name", "team_city"], keep="first")

    base["team_name"] = base["team_name"].astype(str).str.strip()
    base["team_city"] = base["team_city"].astype(str).str.strip()
    out = base.merge(team_map, on=["game_id", "team_name", "team_city"], how="left")

    adv = player_adv[["gameId", "personId", "tsPct", "efgPct", "usgPct", "offRating", "defRating", "netRating", "astPct", "rebPct"]].rename(
        columns={
            "gameId": "game_id",
            "personId": "player_id",
            "tsPct": "ts_pct",
            "efgPct": "efg_pct",
            "usgPct": "usg_pct",
            "offRating": "off_rating",
            "defRating": "def_rating",
            "netRating": "net_rating",
            "astPct": "ast_pct",
            "rebPct": "reb_pct",
        }
    )
    adv["game_id"] = pd.to_numeric(adv["game_id"], errors="coerce").astype("Int64")
    adv["player_id"] = pd.to_numeric(adv["player_id"], errors="coerce").astype("Int64")
    adv = adv.drop_duplicates(subset=["game_id", "player_id"], keep="first")

    out = out.merge(adv, on=["game_id", "player_id"], how="left")

    for col in [
        "minutes_played",
        "points",
        "rebounds",
        "assists",
        "steals",
        "blocks",
        "turnovers",
        "fouls",
        "plus_minus",
        "ts_pct",
        "efg_pct",
        "usg_pct",
        "off_rating",
        "def_rating",
        "net_rating",
        "ast_pct",
        "reb_pct",
    ]:
        if col in out.columns:
            out[col] = pd.to_numeric(out[col], errors="coerce")

    int_cols = ["game_id", "player_id", "team_id", "points", "rebounds", "assists", "steals", "blocks", "turnovers", "fouls", "plus_minus"]
    for col in int_cols:
        out[col] = pd.to_numeric(out[col], errors="coerce").astype("Int64")

    out = out.dropna(subset=["game_id", "player_id", "team_id"]).copy()

    for col in int_cols:
        out[col] = out[col].fillna(0).astype("int64")

    dt = parse_dt(base.get("gameDateTimeEst", pd.Series([None] * len(base))))
    out["season_year"] = dt.dt.year
    out.loc[dt.dt.month >= 10, "season_year"] = out.loc[dt.dt.month >= 10, "season_year"] + 1

    out = out[
        [
            "game_id",
            "player_id",
            "team_id",
            "season_year",
            "minutes_played",
            "points",
            "rebounds",
            "assists",
            "steals",
            "blocks",
            "turnovers",
            "fouls",
            "plus_minus",
            "ts_pct",
            "efg_pct",
            "usg_pct",
            "off_rating",
            "def_rating",
            "net_rating",
            "ast_pct",
            "reb_pct",
        ]
    ].drop_duplicates(subset=["game_id", "player_id"], keep="first")

    out = out[pd.to_numeric(out["season_year"], errors="coerce").fillna(0).astype(int) >= MIN_SEASON_YEAR].copy()
    out = out.drop(columns=["season_year"])

    return out


def build_fact_player_season(player_stats: pd.DataFrame, player_adv: pd.DataFrame) -> pd.DataFrame:
    ps = player_stats.rename(
        columns={
            "personId": "player_id",
            "gameId": "game_id",
            "gameDateTimeEst": "game_date_time",
            "numMinutes": "minutes_played",
            "points": "points",
            "reboundsTotal": "rebounds",
            "assists": "assists",
        }
    ).copy()
    ps["player_id"] = pd.to_numeric(ps["player_id"], errors="coerce").astype("Int64")
    ps["game_id"] = pd.to_numeric(ps["game_id"], errors="coerce").astype("Int64")
    dt = parse_dt(ps["game_date_time"])
    season = dt.dt.year.astype("float64")
    season = season.where(~(dt.dt.month >= 10), season + 1)
    ps["season_year"] = season

    for c in ["minutes_played", "points", "rebounds", "assists"]:
        ps[c] = pd.to_numeric(ps[c], errors="coerce")

    base_agg = (
        ps.dropna(subset=["player_id", "season_year"])
        .groupby(["player_id", "season_year"], as_index=False)
        .agg(
            games_played=("game_id", "nunique"),
            avg_minutes=("minutes_played", "mean"),
            avg_points=("points", "mean"),
            avg_rebounds=("rebounds", "mean"),
            avg_assists=("assists", "mean"),
        )
    )

    adv = player_adv[["personId", "gameDateTimeEst", "tsPct", "gameId"]].rename(
        columns={"personId": "player_id", "gameDateTimeEst": "game_date_time", "tsPct": "ts_pct", "gameId": "game_id"}
    )
    adv["player_id"] = pd.to_numeric(adv["player_id"], errors="coerce").astype("Int64")
    adv["game_id"] = pd.to_numeric(adv["game_id"], errors="coerce").astype("Int64")
    adv["ts_pct"] = pd.to_numeric(adv["ts_pct"], errors="coerce")
    adv_dt = parse_dt(adv["game_date_time"])
    adv["season_year"] = adv_dt.dt.year
    adv.loc[adv_dt.dt.month >= 10, "season_year"] = adv.loc[adv_dt.dt.month >= 10, "season_year"] + 1

    ts_agg = (
        adv.dropna(subset=["player_id", "season_year"])
        .groupby(["player_id", "season_year"], as_index=False)
        .agg(avg_ts_pct=("ts_pct", "mean"))
    )

    out = base_agg.merge(ts_agg, on=["player_id", "season_year"], how="left")
    out["games_started"] = 0
    out["per"] = np.nan
    out["win_shares"] = np.nan
    out["source_tag"] = "kaggle_eoin_nba"
    out["updated_at"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    out["player_id"] = out["player_id"].astype("int64")
    out["season_year"] = out["season_year"].astype("int64")
    out["games_played"] = out["games_played"].fillna(0).astype("int64")
    out["games_started"] = out["games_started"].fillna(0).astype("int64")

    return out[
        [
            "player_id",
            "season_year",
            "games_played",
            "games_started",
            "avg_minutes",
            "avg_points",
            "avg_rebounds",
            "avg_assists",
            "avg_ts_pct",
            "per",
            "win_shares",
            "source_tag",
            "updated_at",
        ]
    ].drop_duplicates(subset=["player_id", "season_year"], keep="first")


def build_fact_team_season(team_stats: pd.DataFrame, team_adv: pd.DataFrame) -> pd.DataFrame:
    ts = team_stats.rename(
        columns={
            "teamId": "team_id",
            "gameId": "game_id",
            "gameDateTimeEst": "game_date_time",
            "win": "win_flag",
        }
    ).copy()
    ts["team_id"] = pd.to_numeric(ts["team_id"], errors="coerce").astype("Int64")
    ts["game_id"] = pd.to_numeric(ts["game_id"], errors="coerce").astype("Int64")

    dt = parse_dt(ts["game_date_time"])
    ts["season_year"] = dt.dt.year
    ts.loc[dt.dt.month >= 10, "season_year"] = ts.loc[dt.dt.month >= 10, "season_year"] + 1
    ts["win_i"] = safe_bool_to_int(ts["win_flag"], default=0)

    base = (
        ts.dropna(subset=["team_id", "season_year"])
        .groupby(["team_id", "season_year"], as_index=False)
        .agg(games=("game_id", "nunique"), wins=("win_i", "sum"))
    )
    base["losses"] = base["games"] - base["wins"]

    ta = team_adv.rename(
        columns={
            "teamId": "team_id",
            "gameDateTimeEst": "game_date_time",
            "offRating": "off_rating",
            "defRating": "def_rating",
            "netRating": "net_rating",
            "pace": "pace",
        }
    ).copy()
    ta["team_id"] = pd.to_numeric(ta["team_id"], errors="coerce").astype("Int64")
    dt2 = parse_dt(ta["game_date_time"])
    ta["season_year"] = dt2.dt.year
    ta.loc[dt2.dt.month >= 10, "season_year"] = ta.loc[dt2.dt.month >= 10, "season_year"] + 1
    for c in ["off_rating", "def_rating", "net_rating", "pace"]:
        ta[c] = pd.to_numeric(ta[c], errors="coerce")

    adv = (
        ta.dropna(subset=["team_id", "season_year"])
        .groupby(["team_id", "season_year"], as_index=False)
        .agg(off_rating=("off_rating", "mean"), def_rating=("def_rating", "mean"), net_rating=("net_rating", "mean"), pace=("pace", "mean"))
    )

    out = base.merge(adv, on=["team_id", "season_year"], how="left")
    out["updated_at"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    out["team_id"] = out["team_id"].astype("int64")
    out["season_year"] = out["season_year"].astype("int64")
    out["wins"] = out["wins"].fillna(0).astype("int64")
    out["losses"] = out["losses"].fillna(0).astype("int64")

    return out[["team_id", "season_year", "wins", "losses", "off_rating", "def_rating", "net_rating", "pace", "updated_at"]].drop_duplicates(
        subset=["team_id", "season_year"], keep="first"
    )


def write_csv(df: pd.DataFrame, path: Path) -> None:
    df.to_csv(path, index=False, encoding="utf-8")


def write_empty_business_tables(out_dir: Path) -> None:
    prediction_snapshot = pd.DataFrame(
        columns=[
            "player_id",
            "predicted_points",
            "predicted_rebounds",
            "predicted_assists",
            "confidence",
            "model_version",
            "created_at",
        ]
    )
    spark_job_run = pd.DataFrame(columns=["job_name", "status", "created_at", "updated_at", "detail"])
    upload_history = pd.DataFrame(columns=["file_name", "rows_count", "status", "created_at"])
    write_csv(prediction_snapshot, out_dir / "prediction_snapshot.csv")
    write_csv(spark_job_run, out_dir / "spark_job_run.csv")
    write_csv(upload_history, out_dir / "upload_history.csv")


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    players = pd.read_csv(RAW_DIR / "Players.csv", low_memory=False)
    team_histories = pd.read_csv(RAW_DIR / "TeamHistories.csv", low_memory=False)
    games = pd.read_csv(RAW_DIR / "Games.csv", low_memory=False)
    player_stats = pd.read_csv(RAW_DIR / "PlayerStatistics.csv", low_memory=False)
    player_adv = pd.read_csv(RAW_DIR / "PlayerStatisticsAdvanced.csv", low_memory=False)
    team_stats = pd.read_csv(RAW_DIR / "TeamStatistics.csv", low_memory=False)
    team_adv = pd.read_csv(RAW_DIR / "TeamStatisticsAdvanced.csv", low_memory=False)

    dim_team = build_dim_team(team_histories, games, team_stats)
    dim_player = build_dim_player(players)
    fact_game = build_fact_game(games)
    fact_player_game = build_fact_player_game(player_stats, player_adv, team_stats)
    fact_player_season = build_fact_player_season(player_stats, player_adv)
    fact_team_season = build_fact_team_season(team_stats, team_adv)

    # Keep a practical range for business dashboards and easier Navicat import.
    fact_game = fact_game[fact_game["season_year"] >= MIN_SEASON_YEAR].copy()
    fact_player_season = fact_player_season[fact_player_season["season_year"] >= MIN_SEASON_YEAR].copy()
    fact_team_season = fact_team_season[fact_team_season["season_year"] >= MIN_SEASON_YEAR].copy()

    # FK safety filtering before export.
    valid_team_ids = set(dim_team["team_id"].tolist())
    valid_player_ids = set(dim_player["player_id"].tolist())
    valid_game_ids = set(fact_game["game_id"].tolist())

    fact_player_game = fact_player_game[
        fact_player_game["game_id"].isin(valid_game_ids)
        & fact_player_game["player_id"].isin(valid_player_ids)
        & fact_player_game["team_id"].isin(valid_team_ids)
    ].copy()

    fact_player_season = fact_player_season[fact_player_season["player_id"].isin(valid_player_ids)].copy()
    fact_team_season = fact_team_season[fact_team_season["team_id"].isin(valid_team_ids)].copy()

    write_csv(dim_team, OUT_DIR / "dim_team.csv")
    write_csv(dim_player, OUT_DIR / "dim_player.csv")
    write_csv(fact_game, OUT_DIR / "fact_game.csv")
    write_csv(fact_player_game, OUT_DIR / "fact_player_game.csv")
    write_csv(fact_player_season, OUT_DIR / "fact_player_season.csv")
    write_csv(fact_team_season, OUT_DIR / "fact_team_season.csv")
    write_empty_business_tables(OUT_DIR)

    summary = pd.DataFrame(
        [
            {"file": "dim_team.csv", "rows": len(dim_team)},
            {"file": "dim_player.csv", "rows": len(dim_player)},
            {"file": "fact_game.csv", "rows": len(fact_game)},
            {"file": "fact_player_game.csv", "rows": len(fact_player_game)},
            {"file": "fact_player_season.csv", "rows": len(fact_player_season)},
            {"file": "fact_team_season.csv", "rows": len(fact_team_season)},
            {"file": "prediction_snapshot.csv", "rows": 0},
            {"file": "spark_job_run.csv", "rows": 0},
            {"file": "upload_history.csv", "rows": 0},
        ]
    )
    write_csv(summary, OUT_DIR / "_summary.csv")
    print(summary.to_string(index=False))
    print(f"\nOutput directory: {OUT_DIR}")


if __name__ == "__main__":
    main()

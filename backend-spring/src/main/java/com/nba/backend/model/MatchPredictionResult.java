package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MatchPredictionResult(
    @JsonProperty("home_team") String homeTeam,
    @JsonProperty("away_team") String awayTeam,
    @JsonProperty("home_win_probability") double homeWinProbability,
    @JsonProperty("away_win_probability") double awayWinProbability,
    double confidence,
    @JsonProperty("key_factors")
    List<String> keyFactors
) {
}

package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatchPredictionRequest(
    @JsonProperty("home_team") String homeTeam,
    @JsonProperty("away_team") String awayTeam
) {
}

package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SeasonPredictionRequest(@JsonProperty("player_id") long playerId) {
}

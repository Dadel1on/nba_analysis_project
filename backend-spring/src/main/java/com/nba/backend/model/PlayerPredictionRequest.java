package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerPredictionRequest(@JsonProperty("player_id") long playerId) {
}

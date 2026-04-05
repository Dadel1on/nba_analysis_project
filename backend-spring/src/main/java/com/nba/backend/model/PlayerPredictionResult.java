package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerPredictionResult(
	@JsonProperty("player_id") long playerId,
	@JsonProperty("predicted_stats") PlayerStats predictedStats,
	double confidence
) {
}

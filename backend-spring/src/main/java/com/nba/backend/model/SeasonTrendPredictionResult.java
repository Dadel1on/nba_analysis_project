package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SeasonTrendPredictionResult(
	@JsonProperty("player_id") long playerId,
	List<SeasonTrendPoint> history,
	List<SeasonTrendPoint> forecast
) {
}

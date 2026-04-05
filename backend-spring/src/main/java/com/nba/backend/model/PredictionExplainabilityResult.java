package com.nba.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PredictionExplainabilityResult(
    @JsonProperty("player_id") long playerId,
    @JsonProperty("model_name") String modelName,
    @JsonProperty("model_version") String modelVersion,
    @JsonProperty("key_factors") List<ExplanationFactor> keyFactors
) {
}

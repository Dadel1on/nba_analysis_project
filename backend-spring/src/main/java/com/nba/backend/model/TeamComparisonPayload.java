package com.nba.backend.model;

import java.util.List;

public record TeamComparisonPayload(List<TeamComparisonMetrics> teams) {
}

package com.nba.backend.model;

import java.util.List;

public record TeamsPayload(List<TeamSummary> teams) {
}

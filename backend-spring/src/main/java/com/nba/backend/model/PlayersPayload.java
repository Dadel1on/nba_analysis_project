package com.nba.backend.model;

import java.util.List;

public record PlayersPayload(List<PlayerSummary> players) {
}

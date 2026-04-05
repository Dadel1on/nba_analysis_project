package com.nba.backend.model;

public record PlayerSummary(long id, String name, String team, String position, PlayerStats stats) {
}

package com.nba.backend.model;

public record TeamComparisonMetrics(String team, double points, double rebounds, double assists, int wins,
                                    double winRate) {
}

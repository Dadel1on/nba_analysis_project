package com.nba.backend.model;

import java.util.List;

public record DashboardOverview(
    DashboardStats stats,
    List<TrendPoint> trend,
    List<PositionDistributionItem> positionDistribution,
    List<RecentGame> recentGames,
    List<TopPlayer> topPlayers
) {
}

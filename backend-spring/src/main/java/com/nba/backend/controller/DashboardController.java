package com.nba.backend.controller;

import com.nba.backend.common.ApiResponse;
import com.nba.backend.model.DashboardOverview;
import com.nba.backend.model.PaginatedPayload;
import com.nba.backend.model.RecentGame;
import com.nba.backend.model.TopPlayer;
import com.nba.backend.service.NbaDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final NbaDataService nbaDataService;

  public DashboardController(NbaDataService nbaDataService) {
    this.nbaDataService = nbaDataService;
  }

  @GetMapping("/overview")
  public ApiResponse<DashboardOverview> getOverview() {
    return ApiResponse.ok(nbaDataService.getDashboardOverview());
  }

  @GetMapping("/games")
  public ApiResponse<PaginatedPayload<RecentGame>> getGames(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    return ApiResponse.ok(nbaDataService.getPaginatedGames(page, limit));
  }

  @GetMapping("/rankings")
  public ApiResponse<PaginatedPayload<TopPlayer>> getRankings(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {
    return ApiResponse.ok(nbaDataService.getPaginatedRankings(page, limit));
  }
}

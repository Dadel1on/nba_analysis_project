package com.nba.backend.controller;

import com.nba.backend.common.ApiResponse;
import com.nba.backend.model.*;
import com.nba.backend.service.NbaDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

  private final NbaDataService nbaDataService;

  public PredictionController(NbaDataService nbaDataService) {
    this.nbaDataService = nbaDataService;
  }

  @PostMapping("/player")
  public ApiResponse<PlayerPredictionResult> predictPlayer(@RequestBody PlayerPredictionRequest request) {
    return ApiResponse.ok(nbaDataService.predictPlayer(request));
  }

  @PostMapping("/match")
  public ApiResponse<MatchPredictionResult> predictMatch(@RequestBody MatchPredictionRequest request) {
    return ApiResponse.ok(nbaDataService.predictMatch(request));
  }

  @PostMapping("/season")
  public ApiResponse<SeasonTrendPredictionResult> predictSeason(@RequestBody SeasonPredictionRequest request) {
    return ApiResponse.ok(nbaDataService.predictSeasonTrend(request));
  }

  @GetMapping("/explain/player")
  public ApiResponse<PredictionExplainabilityResult> explainPlayer(@RequestParam("player_id") long playerId) {
    return ApiResponse.ok(nbaDataService.explainPrediction(playerId));
  }
}

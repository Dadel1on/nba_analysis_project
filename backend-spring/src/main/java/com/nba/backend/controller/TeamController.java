package com.nba.backend.controller;

import com.nba.backend.common.ApiResponse;
import com.nba.backend.model.TeamCompareRequest;
import com.nba.backend.model.TeamComparisonPayload;
import com.nba.backend.model.TeamsPayload;
import com.nba.backend.service.NbaDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

  private final NbaDataService nbaDataService;

  public TeamController(NbaDataService nbaDataService) {
    this.nbaDataService = nbaDataService;
  }

  @GetMapping
  public ApiResponse<TeamsPayload> getTeams() {
    return ApiResponse.ok(nbaDataService.getTeams());
  }

  @PostMapping("/compare")
  public ApiResponse<TeamComparisonPayload> compare(@RequestBody TeamCompareRequest request) {
    return ApiResponse.ok(nbaDataService.compareTeams(request));
  }
}

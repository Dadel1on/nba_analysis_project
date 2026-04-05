package com.nba.backend.controller;

import com.nba.backend.common.ApiResponse;
import com.nba.backend.model.PlayerSummary;
import com.nba.backend.model.PlayersPayload;
import com.nba.backend.service.NbaDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

  private final NbaDataService nbaDataService;

  public PlayerController(NbaDataService nbaDataService) {
    this.nbaDataService = nbaDataService;
  }

  @GetMapping
  public ApiResponse<PlayersPayload> searchPlayers(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "limit", defaultValue = "20") int limit
  ) {
    return ApiResponse.ok(nbaDataService.searchPlayers(name, page, limit));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PlayerSummary>> getPlayerById(@PathVariable("id") long id) {
    PlayerSummary player = nbaDataService.getPlayerById(id);
    if (player == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(404, "Player not found", null));
    }
    return ResponseEntity.ok(ApiResponse.ok(player));
  }
}

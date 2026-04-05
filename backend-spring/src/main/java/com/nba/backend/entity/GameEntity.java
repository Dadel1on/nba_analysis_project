package com.nba.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fact_game")
public class GameEntity {

  @Id
  @Column(name = "game_id")
  private Long id;

  @Column(name = "game_date", nullable = false)
  private LocalDate gameDate;

  @Column(name = "season_year", nullable = false)
  private Integer season;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "home_team_id", nullable = false)
  private TeamEntity homeTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "away_team_id", nullable = false)
  private TeamEntity awayTeam;

  @Column(name = "home_points")
  private Integer homePoints;

  @Column(name = "away_points")
  private Integer awayPoints;

  @Column(name = "game_status", length = 40)
  private String gameStatus;

  @Column(name = "attendance")
  private Integer attendance;

  @Column(name = "arena_name", length = 100)
  private String arenaName;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getGameDate() {
    return gameDate;
  }

  public void setGameDate(LocalDate gameDate) {
    this.gameDate = gameDate;
  }

  public Integer getSeason() {
    return season;
  }

  public void setSeason(Integer season) {
    this.season = season;
  }

  public TeamEntity getHomeTeam() {
    return homeTeam;
  }

  public void setHomeTeam(TeamEntity homeTeam) {
    this.homeTeam = homeTeam;
  }

  public TeamEntity getAwayTeam() {
    return awayTeam;
  }

  public void setAwayTeam(TeamEntity awayTeam) {
    this.awayTeam = awayTeam;
  }

  public Integer getHomePoints() {
    return homePoints;
  }

  public void setHomePoints(Integer homePoints) {
    this.homePoints = homePoints;
  }

  public Integer getAwayPoints() {
    return awayPoints;
  }

  public void setAwayPoints(Integer awayPoints) {
    this.awayPoints = awayPoints;
  }

  public String getGameStatus() {
    return gameStatus;
  }

  public void setGameStatus(String gameStatus) {
    this.gameStatus = gameStatus;
  }

  public Integer getAttendance() {
    return attendance;
  }

  public void setAttendance(Integer attendance) {
    this.attendance = attendance;
  }

  public String getArenaName() {
    return arenaName;
  }

  public void setArenaName(String arenaName) {
    this.arenaName = arenaName;
  }
}

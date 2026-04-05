package com.nba.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fact_player_season")
public class PlayerSeasonEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id", nullable = false)
  private PlayerEntity player;

  @Column(name = "season_year", nullable = false)
  private Integer seasonYear;

  @Column(name = "games_played")
  private Integer gamesPlayed = 0;

  @Column(name = "games_started")
  private Integer gamesStarted = 0;

  @Column(name = "avg_minutes")
  private Double avgMinutes;

  @Column(name = "avg_points")
  private Double avgPoints;

  @Column(name = "avg_rebounds")
  private Double avgRebounds;

  @Column(name = "avg_assists")
  private Double avgAssists;

  @Column(name = "avg_ts_pct")
  private Double avgTsPct;

  @Column(name = "per")
  private Double per;
  @Column(name = "win_shares")
  private Double winShares;

  @Column(name = "source_tag", length = 40)
  private String sourceTag;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PlayerEntity getPlayer() {
    return player;
  }

  public void setPlayer(PlayerEntity player) {
    this.player = player;
  }

  public Integer getSeasonYear() {
    return seasonYear;
  }

  public void setSeasonYear(Integer seasonYear) {
    this.seasonYear = seasonYear;
  }

  public Integer getGamesPlayed() {
    return gamesPlayed;
  }

  public void setGamesPlayed(Integer gamesPlayed) {
    this.gamesPlayed = gamesPlayed;
  }

  public Integer getGamesStarted() {
    return gamesStarted;
  }

  public void setGamesStarted(Integer gamesStarted) {
    this.gamesStarted = gamesStarted;
  }

  public Double getAvgMinutes() {
    return avgMinutes;
  }

  public void setAvgMinutes(Double avgMinutes) {
    this.avgMinutes = avgMinutes;
  }

  public Double getAvgPoints() {
    return avgPoints;
  }

  public void setAvgPoints(Double avgPoints) {
    this.avgPoints = avgPoints;
  }

  public Double getAvgRebounds() {
    return avgRebounds;
  }

  public void setAvgRebounds(Double avgRebounds) {
    this.avgRebounds = avgRebounds;
  }

  public Double getAvgAssists() {
    return avgAssists;
  }

  public void setAvgAssists(Double avgAssists) {
    this.avgAssists = avgAssists;
  }

  public Double getAvgTsPct() {
    return avgTsPct;
  }

  public void setAvgTsPct(Double avgTsPct) {
    this.avgTsPct = avgTsPct;
  }

  public Double getPer() {
    return per;
  }

  public void setPer(Double per) {
    this.per = per;
  }

  public Double getWinShares() {
    return winShares;
  }

  public void setWinShares(Double winShares) {
    this.winShares = winShares;
  }

  public String getSourceTag() {
    return sourceTag;
  }

  public void setSourceTag(String sourceTag) {
    this.sourceTag = sourceTag;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}

package com.nba.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fact_team_season")
public class TeamSeasonEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  private TeamEntity team;

  @Column(name = "season_year", nullable = false)
  private Integer seasonYear;

  private Integer wins = 0;
  private Integer losses = 0;

  @Column(name = "off_rating")
  private Double offRating;

  @Column(name = "def_rating")
  private Double defRating;

  @Column(name = "net_rating")
  private Double netRating;

  private Double pace;

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

  public TeamEntity getTeam() {
    return team;
  }

  public void setTeam(TeamEntity team) {
    this.team = team;
  }

  public Integer getSeasonYear() {
    return seasonYear;
  }

  public void setSeasonYear(Integer seasonYear) {
    this.seasonYear = seasonYear;
  }

  public Integer getWins() {
    return wins;
  }

  public void setWins(Integer wins) {
    this.wins = wins;
  }

  public Integer getLosses() {
    return losses;
  }

  public void setLosses(Integer losses) {
    this.losses = losses;
  }

  public Double getOffRating() {
    return offRating;
  }

  public void setOffRating(Double offRating) {
    this.offRating = offRating;
  }

  public Double getDefRating() {
    return defRating;
  }

  public void setDefRating(Double defRating) {
    this.defRating = defRating;
  }

  public Double getNetRating() {
    return netRating;
  }

  public void setNetRating(Double netRating) {
    this.netRating = netRating;
  }

  public Double getPace() {
    return pace;
  }

  public void setPace(Double pace) {
    this.pace = pace;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}

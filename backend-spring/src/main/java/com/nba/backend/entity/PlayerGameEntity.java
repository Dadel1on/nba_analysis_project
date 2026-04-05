package com.nba.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fact_player_game")
public class PlayerGameEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id", nullable = false)
  private GameEntity game;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id", nullable = false)
  private PlayerEntity player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  private TeamEntity team;

  @Column(name = "minutes_played")
  private Double minutesPlayed;

  private Integer points = 0;
  private Integer rebounds = 0;
  private Integer assists = 0;
  private Integer steals = 0;
  private Integer blocks = 0;
  private Integer turnovers = 0;
  private Integer fouls = 0;
  @Column(name = "plus_minus")
  private Integer plusMinus = 0;

  @Column(name = "ts_pct")
  private Double tsPct;

  @Column(name = "efg_pct")
  private Double efgPct;

  @Column(name = "usg_pct")
  private Double usgPct;

  @Column(name = "off_rating")
  private Double offRating;

  @Column(name = "def_rating")
  private Double defRating;

  @Column(name = "net_rating")
  private Double netRating;

  @Column(name = "ast_pct")
  private Double astPct;

  @Column(name = "reb_pct")
  private Double rebPct;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public GameEntity getGame() {
    return game;
  }

  public void setGame(GameEntity game) {
    this.game = game;
  }

  public PlayerEntity getPlayer() {
    return player;
  }

  public void setPlayer(PlayerEntity player) {
    this.player = player;
  }

  public TeamEntity getTeam() {
    return team;
  }

  public void setTeam(TeamEntity team) {
    this.team = team;
  }

  public Double getMinutesPlayed() {
    return minutesPlayed;
  }

  public void setMinutesPlayed(Double minutesPlayed) {
    this.minutesPlayed = minutesPlayed;
  }

  public Integer getPoints() {
    return points;
  }

  public void setPoints(Integer points) {
    this.points = points;
  }

  public Integer getRebounds() {
    return rebounds;
  }

  public void setRebounds(Integer rebounds) {
    this.rebounds = rebounds;
  }

  public Integer getAssists() {
    return assists;
  }

  public void setAssists(Integer assists) {
    this.assists = assists;
  }

  public Integer getSteals() {
    return steals;
  }

  public void setSteals(Integer steals) {
    this.steals = steals;
  }

  public Integer getBlocks() {
    return blocks;
  }

  public void setBlocks(Integer blocks) {
    this.blocks = blocks;
  }

  public Integer getTurnovers() {
    return turnovers;
  }

  public void setTurnovers(Integer turnovers) {
    this.turnovers = turnovers;
  }

  public Integer getFouls() {
    return fouls;
  }

  public void setFouls(Integer fouls) {
    this.fouls = fouls;
  }

  public Integer getPlusMinus() {
    return plusMinus;
  }

  public void setPlusMinus(Integer plusMinus) {
    this.plusMinus = plusMinus;
  }

  public Double getTsPct() {
    return tsPct;
  }

  public void setTsPct(Double tsPct) {
    this.tsPct = tsPct;
  }

  public Double getEfgPct() {
    return efgPct;
  }

  public void setEfgPct(Double efgPct) {
    this.efgPct = efgPct;
  }

  public Double getUsgPct() {
    return usgPct;
  }

  public void setUsgPct(Double usgPct) {
    this.usgPct = usgPct;
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

  public Double getAstPct() {
    return astPct;
  }

  public void setAstPct(Double astPct) {
    this.astPct = astPct;
  }

  public Double getRebPct() {
    return rebPct;
  }

  public void setRebPct(Double rebPct) {
    this.rebPct = rebPct;
  }
}

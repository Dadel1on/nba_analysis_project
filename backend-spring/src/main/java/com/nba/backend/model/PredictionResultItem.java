package com.nba.backend.model;

public class PredictionResultItem {
  private Long id;
  private Long playerId;
  private String playerName;
  private Double predictedPoints;
  private Double predictedRebounds;
  private Double predictedAssists;
  private Double confidence;
  private String modelVersion;
  private String createdAt;

  public PredictionResultItem() {}

  public PredictionResultItem(Long id, Long playerId, String playerName, Double predictedPoints, 
                              Double predictedRebounds, Double predictedAssists, Double confidence, 
                              String modelVersion, String createdAt) {
    this.id = id;
    this.playerId = playerId;
    this.playerName = playerName;
    this.predictedPoints = predictedPoints;
    this.predictedRebounds = predictedRebounds;
    this.predictedAssists = predictedAssists;
    this.confidence = confidence;
    this.modelVersion = modelVersion;
    this.createdAt = createdAt;
  }

  // Getters and Setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getPlayerId() { return playerId; }
  public void setPlayerId(Long playerId) { this.playerId = playerId; }
  public String getPlayerName() { return playerName; }
  public void setPlayerName(String playerName) { this.playerName = playerName; }
  public Double getPredictedPoints() { return predictedPoints; }
  public void setPredictedPoints(Double predictedPoints) { this.predictedPoints = predictedPoints; }
  public Double getPredictedRebounds() { return predictedRebounds; }
  public void setPredictedRebounds(Double predictedRebounds) { this.predictedRebounds = predictedRebounds; }
  public Double getPredictedAssists() { return predictedAssists; }
  public void setPredictedAssists(Double predictedAssists) { this.predictedAssists = predictedAssists; }
  public Double getConfidence() { return confidence; }
  public void setConfidence(Double confidence) { this.confidence = confidence; }
  public String getModelVersion() { return modelVersion; }
  public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

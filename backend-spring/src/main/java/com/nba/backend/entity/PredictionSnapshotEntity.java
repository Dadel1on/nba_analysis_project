package com.nba.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_snapshot")
public class PredictionSnapshotEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id", nullable = false)
  private PlayerEntity player;

  @Column(name = "predicted_points", nullable = false)
  private Double predictedPoints;

  @Column(name = "predicted_rebounds", nullable = false)
  private Double predictedRebounds;

  @Column(name = "predicted_assists", nullable = false)
  private Double predictedAssists;

  @Column(nullable = false)
  private Double confidence;

  @Column(name = "model_version", nullable = false, length = 60)
  private String modelVersion;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
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

  public Double getPredictedPoints() {
    return predictedPoints;
  }

  public void setPredictedPoints(Double predictedPoints) {
    this.predictedPoints = predictedPoints;
  }

  public Double getPredictedRebounds() {
    return predictedRebounds;
  }

  public void setPredictedRebounds(Double predictedRebounds) {
    this.predictedRebounds = predictedRebounds;
  }

  public Double getPredictedAssists() {
    return predictedAssists;
  }

  public void setPredictedAssists(Double predictedAssists) {
    this.predictedAssists = predictedAssists;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  public String getModelVersion() {
    return modelVersion;
  }

  public void setModelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}

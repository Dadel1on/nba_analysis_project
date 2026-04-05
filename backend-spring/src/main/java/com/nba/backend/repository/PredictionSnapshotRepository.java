package com.nba.backend.repository;

import com.nba.backend.entity.PredictionSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PredictionSnapshotRepository extends JpaRepository<PredictionSnapshotEntity, Long> {
  Optional<PredictionSnapshotEntity> findTopByPlayer_IdOrderByCreatedAtDesc(Long playerId);
}

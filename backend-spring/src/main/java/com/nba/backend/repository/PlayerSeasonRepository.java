package com.nba.backend.repository;

import com.nba.backend.entity.PlayerSeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerSeasonRepository extends JpaRepository<PlayerSeasonEntity, Long> {
    List<PlayerSeasonEntity> findByPlayer_Id(Long playerId);
    Optional<PlayerSeasonEntity> findByPlayer_IdAndSeasonYear(Long playerId, Integer seasonYear);
}

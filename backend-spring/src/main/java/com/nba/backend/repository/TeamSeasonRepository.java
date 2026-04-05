package com.nba.backend.repository;

import com.nba.backend.entity.TeamSeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TeamSeasonRepository extends JpaRepository<TeamSeasonEntity, Long> {
    List<TeamSeasonEntity> findByTeam_Id(Long teamId);
    Optional<TeamSeasonEntity> findByTeam_IdAndSeasonYear(Long teamId, Integer seasonYear);
}

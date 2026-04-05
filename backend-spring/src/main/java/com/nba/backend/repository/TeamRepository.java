package com.nba.backend.repository;

import com.nba.backend.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
  Optional<TeamEntity> findByNameIgnoreCase(String name);
}

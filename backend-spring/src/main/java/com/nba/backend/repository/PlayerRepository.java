package com.nba.backend.repository;

import com.nba.backend.entity.PlayerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
  List<PlayerEntity> findByNameContainingIgnoreCase(String name);

  Page<PlayerEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

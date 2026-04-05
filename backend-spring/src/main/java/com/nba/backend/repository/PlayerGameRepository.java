package com.nba.backend.repository;

import com.nba.backend.entity.PlayerGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerGameRepository extends JpaRepository<PlayerGameEntity, Long> {
    List<PlayerGameEntity> findByPlayer_Id(Long playerId);
    List<PlayerGameEntity> findByGame_Id(Long gameId);
}

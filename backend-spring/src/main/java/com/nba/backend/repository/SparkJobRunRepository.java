package com.nba.backend.repository;

import com.nba.backend.entity.SparkJobRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparkJobRunRepository extends JpaRepository<SparkJobRunEntity, Long> {
    List<SparkJobRunEntity> findTop20ByOrderByCreatedAtDesc();
}

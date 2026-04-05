package com.nba.backend.repository;

import com.nba.backend.entity.UploadHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadHistoryRepository extends JpaRepository<UploadHistoryEntity, Long> {
}

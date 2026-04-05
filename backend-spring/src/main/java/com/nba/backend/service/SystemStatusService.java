package com.nba.backend.service;

import com.nba.backend.model.DataSourceStatusResult;
import com.nba.backend.repository.GameRepository;
import com.nba.backend.repository.PlayerRepository;
import com.nba.backend.repository.PredictionSnapshotRepository;
import com.nba.backend.repository.TeamRepository;
import com.nba.backend.repository.UploadHistoryRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemStatusService {

  private final Environment environment;
  private final DataSource dataSource;
  private final PlayerRepository playerRepository;
  private final GameRepository gameRepository;
  private final TeamRepository teamRepository;
  private final UploadHistoryRepository uploadHistoryRepository;
  private final PredictionSnapshotRepository predictionSnapshotRepository;

  public SystemStatusService(Environment environment,
                             DataSource dataSource,
                             PlayerRepository playerRepository,
                             GameRepository gameRepository,
                             TeamRepository teamRepository,
                             UploadHistoryRepository uploadHistoryRepository,
                             PredictionSnapshotRepository predictionSnapshotRepository) {
    this.environment = environment;
    this.dataSource = dataSource;
    this.playerRepository = playerRepository;
    this.gameRepository = gameRepository;
    this.teamRepository = teamRepository;
    this.uploadHistoryRepository = uploadHistoryRepository;
    this.predictionSnapshotRepository = predictionSnapshotRepository;
  }

  public DataSourceStatusResult getDataSourceStatus() {
    List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
    String databaseProduct = "unknown";
    String databaseUrl = "unknown";

    try (Connection connection = dataSource.getConnection()) {
      databaseProduct = connection.getMetaData().getDatabaseProductName();
      databaseUrl = maskJdbcUrl(connection.getMetaData().getURL());
    } catch (Exception ignored) {
      // Keep defaults so status endpoint remains available even when DB metadata is temporarily unavailable.
    }

    Map<String, Long> tableCounts = new LinkedHashMap<>();
    tableCounts.put("teams", teamRepository.count());
    tableCounts.put("players", playerRepository.count());
    tableCounts.put("games", gameRepository.count());
    tableCounts.put("upload_history", uploadHistoryRepository.count());
    tableCounts.put("prediction_snapshot", predictionSnapshotRepository.count());

    return new DataSourceStatusResult(
        activeProfiles,
        databaseProduct,
        databaseUrl,
        tableCounts,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    );
  }

  private String maskJdbcUrl(String url) {
    if (url == null || url.isBlank()) {
      return "unknown";
    }

    int queryIndex = url.indexOf('?');
    if (queryIndex < 0) {
      return url;
    }

    return url.substring(0, queryIndex);
  }
}

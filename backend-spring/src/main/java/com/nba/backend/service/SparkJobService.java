package com.nba.backend.service;

import com.nba.backend.entity.PredictionSnapshotEntity;
import com.nba.backend.entity.SparkJobRunEntity;
import com.nba.backend.model.PredictionResultItem;
import com.nba.backend.model.SparkJobRunItem;
import com.nba.backend.repository.PlayerRepository;
import com.nba.backend.repository.PredictionSnapshotRepository;
import com.nba.backend.repository.SparkJobRunRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

@Service
public class SparkJobService {

  private static final int DETAIL_MAX_LENGTH = 8000;

  @Value("${app.spark.enabled:false}")
  private boolean sparkEnabled;

  @Value("${app.spark.submit-command:}")
  private String submitCommand;

  @Value("${app.spark.working-dir:.}")
  private String workingDir;

  @Value("${app.spark.timeout-seconds:1800}")
  private int timeoutSeconds;

  @Value("${app.spark.output-path:target/spark_predictions}")
  private String outputPath;

  private final SparkJobRunRepository sparkJobRunRepository;
  private final PredictionSnapshotRepository predictionSnapshotRepository;
  private final PlayerRepository playerRepository;

  public SparkJobService(SparkJobRunRepository sparkJobRunRepository,
                        PredictionSnapshotRepository predictionSnapshotRepository,
                        PlayerRepository playerRepository) {
    this.sparkJobRunRepository = sparkJobRunRepository;
    this.predictionSnapshotRepository = predictionSnapshotRepository;
    this.playerRepository = playerRepository;
  }

  @Async
  public void runSparkJobAsync() {
    String jobName = "NBA_MLlib_Analysis";
    SparkJobRunEntity run = new SparkJobRunEntity();
    run.setJobName(jobName);
    run.setStatus("running");
    run.setCreatedAt(LocalDateTime.now());
    run.setUpdatedAt(LocalDateTime.now());
    run.setDetail("Job started at " + nowText());
    run = sparkJobRunRepository.save(run);

    if (!sparkEnabled) {
      run.setStatus("skipped");
      run.setDetail("Spark is disabled in configuration.");
      run.setUpdatedAt(LocalDateTime.now());
      persistRun(run);
      return;
    }

    if (submitCommand == null || submitCommand.isBlank()) {
      // Mock successful execution if no command is provided
      run.setStatus("success");
      run.setDetail("Mock execution successful (no command provided).");
      run.setUpdatedAt(LocalDateTime.now());
      persistRun(run);
      return;
    }

    try {
      ProcessBuilder pb = new ProcessBuilder(submitCommand.split("\\s+"));
      pb.directory(new File(workingDir));
      pb.redirectErrorStream(true);

      Process process = pb.start();
      ExecutorService outputReaderExecutor = Executors.newSingleThreadExecutor();
      Future<String> outputFuture = outputReaderExecutor.submit(() -> {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
          String line;
          while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
          }
        }
        return output.toString();
      });

      boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
      String output = readProcessOutput(outputFuture, finished ? 5 : 1);

      if (!finished) {
        process.destroyForcibly();
        run.setStatus("failed");
        run.setDetail("Job timed out after " + timeoutSeconds + " seconds.\n\n" + output);
      } else if (process.exitValue() == 0) {
        run.setStatus("success");
        run.setDetail("Execution successful.\n\n" + output);
        // Synchronize results
        syncSparkResults();
      } else {
        run.setStatus("failed");
        run.setDetail("Execution failed with exit code " + process.exitValue() + ".\n\n" + output);
      }

      outputReaderExecutor.shutdownNow();
    } catch (Exception e) {
      run.setStatus("failed");
      run.setDetail("Exception during execution: " + e.getMessage());
    } finally {
      run.setUpdatedAt(LocalDateTime.now());
      persistRun(run);
    }
  }

  @Transactional
  public void syncSparkResults() {
    Path dir = Paths.get(workingDir, outputPath);
    if (!Files.exists(dir)) {
      return;
    }

    try (Stream<Path> files = Files.list(dir)) {
      Path csvFile = files.filter(p -> p.toString().endsWith(".csv")).findFirst().orElse(null);
      if (csvFile == null) {
        return;
      }

      List<PredictionSnapshotEntity> snapshots = new ArrayList<>();
      try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
        String header = reader.readLine(); // skip header
        String line;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split(",");
          if (parts.length >= 7) {
            long playerId = Long.parseLong(parts[0]);
            if (!playerRepository.existsById(playerId)) {
              continue;
            }
            PredictionSnapshotEntity s = new PredictionSnapshotEntity();
            s.setPlayer(playerRepository.getReferenceById(playerId));
            s.setPredictedPoints(Double.parseDouble(parts[1]));
            s.setPredictedRebounds(Double.parseDouble(parts[2]));
            s.setPredictedAssists(Double.parseDouble(parts[3]));
            s.setConfidence(Double.parseDouble(parts[4]));
            s.setModelVersion(parts[5]);
            s.setCreatedAt(parseDateTimeOrNow(parts[6]));
            snapshots.add(s);
          }
        }
      }

      if (!snapshots.isEmpty()) {
        predictionSnapshotRepository.deleteAll();
        predictionSnapshotRepository.saveAll(snapshots);
      }
    } catch (Exception e) {
      System.err.println("Failed to sync Spark results: " + e.getMessage());
    }
  }

  public List<SparkJobRunItem> getJobHistory() {
    return sparkJobRunRepository.findTop20ByOrderByCreatedAtDesc().stream()
        .map(e -> new SparkJobRunItem(
            e.getId(),
            e.getJobName(),
            e.getStatus(),
            formatDateTime(e.getCreatedAt()),
            formatDateTime(e.getUpdatedAt()),
            e.getDetail()))
        .toList();
  }

  public List<PredictionResultItem> getLatestPredictions() {
    return predictionSnapshotRepository.findAll().stream()
        .map(e -> new PredictionResultItem(
            e.getId(),
            e.getPlayer().getId(),
            e.getPlayer().getName(),
            e.getPredictedPoints(),
            e.getPredictedRebounds(),
            e.getPredictedAssists(),
            e.getConfidence(),
            e.getModelVersion(),
            formatDateTime(e.getCreatedAt())))
        .toList();
  }

  private LocalDateTime parseDateTimeOrNow(String value) {
    if (value == null || value.isBlank()) {
      return LocalDateTime.now();
    }
    try {
      return LocalDateTime.parse(value);
    } catch (Exception ignored) {
      try {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      } catch (Exception ignoredAgain) {
        return LocalDateTime.now();
      }
    }
  }

  private String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return null;
    }
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private String nowText() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private void persistRun(SparkJobRunEntity run) {
    run.setDetail(truncateDetail(run.getDetail()));
    sparkJobRunRepository.save(run);
  }

  private String truncateDetail(String detail) {
    if (detail == null || detail.length() <= DETAIL_MAX_LENGTH) {
      return detail;
    }

    int keep = DETAIL_MAX_LENGTH - 80;
    int headKeep = Math.max(0, keep / 3);
    int tailKeep = Math.max(0, keep - headKeep);

    String head = detail.substring(0, Math.min(headKeep, detail.length()));
    String tail = detail.substring(Math.max(0, detail.length() - tailKeep));

    return head + "\n...[output truncated]...\n" + tail;
  }

  private String readProcessOutput(Future<String> outputFuture, int timeoutSeconds) {
    try {
      return outputFuture.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      return "[output read timed out]";
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "[output read interrupted]";
    } catch (ExecutionException e) {
      return "[failed to read output: " + e.getMessage() + "]";
    }
  }
}

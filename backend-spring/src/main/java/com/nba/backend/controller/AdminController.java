package com.nba.backend.controller;

import com.nba.backend.common.ApiResponse;
import com.nba.backend.model.DataSourceStatusResult;
import com.nba.backend.model.PredictionResultItem;
import com.nba.backend.model.SparkJobRunItem;
import com.nba.backend.model.UploadHistoryItem;
import com.nba.backend.service.NbaDataService;
import com.nba.backend.service.SparkJobService;
import com.nba.backend.service.SystemStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final NbaDataService nbaDataService;
  private final SystemStatusService systemStatusService;
  private final SparkJobService sparkJobService;

  public AdminController(NbaDataService nbaDataService,
                        SystemStatusService systemStatusService,
                        SparkJobService sparkJobService) {
    this.nbaDataService = nbaDataService;
    this.systemStatusService = systemStatusService;
    this.sparkJobService = sparkJobService;
  }

  @PostMapping("/upload")
  public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
    String filename = file.getOriginalFilename() == null ? "uploaded.csv" : file.getOriginalFilename();
    int rows = countCsvRows(file);
    nbaDataService.addUploadHistory(filename, rows, "success");

    return ApiResponse.ok(Map.of(
        "fileName", filename,
        "rows", rows,
        "status", "success"
    ));
  }

  @GetMapping("/upload/history")
  public ApiResponse<List<UploadHistoryItem>> getUploadHistory() {
    return ApiResponse.ok(nbaDataService.getUploadHistory());
  }

  @PostMapping("/spark/run")
  public ApiResponse<String> runSparkJob() {
    sparkJobService.runSparkJobAsync();
    return ApiResponse.ok("Job submitted.");
  }

  @GetMapping("/spark/history")
  public ApiResponse<List<SparkJobRunItem>> getSparkHistory() {
    return ApiResponse.ok(sparkJobService.getJobHistory());
  }

  @GetMapping("/spark/predictions")
  public ApiResponse<List<PredictionResultItem>> getSparkPredictions() {
    return ApiResponse.ok(sparkJobService.getLatestPredictions());
  }

  @GetMapping("/system/status")
  public ApiResponse<DataSourceStatusResult> getSystemStatus() {
    return ApiResponse.ok(systemStatusService.getDataSourceStatus());
  }

  private int countCsvRows(MultipartFile file) throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      int count = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          count++;
        }
      }
      return Math.max(0, count - 1);
    }
  }
}

package com.nba.backend.model;

import java.util.List;
import java.util.Map;

public record DataSourceStatusResult(
    List<String> activeProfiles,
    String databaseProduct,
    String databaseUrl,
    Map<String, Long> tableCounts,
    String serverTime
) {
}

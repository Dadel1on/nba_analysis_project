package com.nba.backend.model;

import java.util.List;

public record TeamCompareRequest(List<String> teams, Integer season, String metric) {
}

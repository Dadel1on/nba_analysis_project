package com.nba.backend.model;

import java.util.List;

public record PaginatedPayload<T>(List<T> list, long total) {
}

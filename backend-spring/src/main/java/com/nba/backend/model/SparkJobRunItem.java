package com.nba.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SparkJobRunItem {
    private Long id;
    private String jobName;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String detail;
}

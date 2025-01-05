package com.moea.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExperimentDTO {
    private Long id;
    private UUID groupId;
    private int evaluations;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> algorithms;
    private List<String> problems;
    private List<String> metrics;
}

package com.moea.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExperimentDTO {
    private Long id;
    private int evaluations;
    private String status;
    private List<String> algorithms;
    private List<String> problems;
    private List<String> metrics;
}

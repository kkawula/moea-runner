package com.moea.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExperimentRequestDTO {
    private int evaluations;
    private List<String> algorithms;
    private List<String> problems;
    private List<String> metrics;
}

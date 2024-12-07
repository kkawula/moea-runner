package com.moea.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ExperimentDTO(int evaluations,
                            List<String> algorithms,
                            List<String> problems,
                            List<String> metrics) {
}

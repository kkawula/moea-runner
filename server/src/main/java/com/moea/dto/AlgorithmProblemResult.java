package com.moea.dto;

import lombok.*;
import org.moeaframework.analysis.collector.Observations;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AlgorithmProblemResult {
    private String algorithmName;
    private String problemName;
    private Observations observations;
}

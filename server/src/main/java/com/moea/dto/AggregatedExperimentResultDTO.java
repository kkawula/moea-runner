package com.moea.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AggregatedExperimentResultDTO {
    private String problem;
    private String algorithm;
    private String metric;
    private int iteration;
    private AggregatedStats result;
}

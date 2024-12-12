package com.moea.dto;

import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExperimentResultDTO {
    private int iteration;
    private String metric;
    private double result;
}

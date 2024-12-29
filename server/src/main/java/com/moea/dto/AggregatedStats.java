package com.moea.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AggregatedStats {
    private double mean;
    private double median;
    private double stdDev;
}

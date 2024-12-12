package com.moea.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@Table(name = "experiment_metrics")
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Experiment experiment;

    @Column(name = "metric_name")
    private String metricName;
}

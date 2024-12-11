package com.moea.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@ToString
@Table(name = "experiment_metrics")
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Experiment experiment;

    @Column(name = "metric_name")
    private String metricName;
}

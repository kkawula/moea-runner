package com.moea.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Setter
@Getter
@Builder
@Table(name = "experiments_metrics_results")
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @Column(name = "problem")
    private String problem;

    @Column(name = "algorithm")
    private String algorithm;

    @Column(name = "metric")
    private String metric;

    @Column(name = "iteration")
    private int iteration;

    @Column(name = "result")
    private double result;
}

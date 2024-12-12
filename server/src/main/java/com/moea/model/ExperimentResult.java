package com.moea.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Setter
@Getter
@Builder
@ToString
@Table(name = "experiments_metrics_results")
@AllArgsConstructor
@NoArgsConstructor
public class ExperimentResult {

    @EmbeddedId
    private ExperimentResultId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("experimentId")
    private Experiment experiment;

    @Column(name = "metric", insertable = false, updatable = false)
    private String metric;

    @Column(name = "iteration", insertable = false, updatable = false)
    private int iteration;

    @Column(name = "result")
    private double result;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentResult that = (ExperimentResult) o;
        return getIteration() == that.getIteration() && Objects.equals(getExperiment(), that.getExperiment()) && Objects.equals(getMetric(), that.getMetric());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExperiment(), getMetric(), getIteration());
    }
}

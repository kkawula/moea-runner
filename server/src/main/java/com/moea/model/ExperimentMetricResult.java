package com.moea.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class ExperimentMetricResult {

    @EmbeddedId
    private ExperimentMetricResultId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("experimentId")
    @JsonBackReference
    private Experiment experiment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("metricId")
    @JsonBackReference
    private Metric metric;

    @Column(name = "iteration", insertable = false, updatable = false)
    private int iteration;

    @Column(name = "result")
    private double result;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentMetricResult that = (ExperimentMetricResult) o;
        return getIteration() == that.getIteration() && Objects.equals(getExperiment(), that.getExperiment()) && Objects.equals(getMetric(), that.getMetric());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExperiment(), getMetric(), getIteration());
    }
}

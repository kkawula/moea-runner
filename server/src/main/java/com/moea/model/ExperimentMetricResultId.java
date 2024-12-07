package com.moea.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ExperimentMetricResultId implements Serializable {
    @Column(name = "experiment_id")
    private Long experimentId;

    @Column(name = "metric_id")
    private Long metricId;

    @Column(name = "iteration")
    private int iteration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentMetricResultId that = (ExperimentMetricResultId) o;
        return Objects.equals(getExperimentId(), that.getExperimentId()) && Objects.equals(getMetricId(), that.getMetricId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExperimentId(), getMetricId());
    }
}

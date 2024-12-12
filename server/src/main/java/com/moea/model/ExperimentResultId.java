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
public class ExperimentResultId implements Serializable {
    @Column(name = "experiment_id")
    private Long experimentId;

    @Column(name = "metric")
    private String metric;

    @Column(name = "iteration")
    private int iteration;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentResultId that = (ExperimentResultId) o;
        return Objects.equals(getExperimentId(), that.getExperimentId()) && Objects.equals(getMetric(), that.getMetric());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExperimentId(), getMetric());
    }
}

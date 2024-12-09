package com.moea.repository;

import com.moea.model.ExperimentMetricResult;

import java.util.List;

public interface ExperimentResultsRepositoryCustom {
    List<ExperimentMetricResult> getResults(String experimentId);
}

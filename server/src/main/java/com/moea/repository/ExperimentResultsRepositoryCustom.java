package com.moea.repository;

import com.moea.model.ExperimentResult;

import java.util.List;

public interface ExperimentResultsRepositoryCustom {
    List<ExperimentResult> getResults(String experimentId);
}

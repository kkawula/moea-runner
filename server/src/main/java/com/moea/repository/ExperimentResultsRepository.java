package com.moea.repository;

import com.moea.model.ExperimentMetricResult;
import com.moea.model.ExperimentMetricResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentResultsRepository extends JpaRepository<ExperimentMetricResult, ExperimentMetricResultId>, ExperimentResultsRepositoryCustom {

}

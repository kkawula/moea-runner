package com.moea.repository;

import com.moea.model.ExperimentResult;
import com.moea.model.ExperimentResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentResultsRepository extends JpaRepository<ExperimentResult, ExperimentResultId>, ExperimentResultsRepositoryCustom {

}

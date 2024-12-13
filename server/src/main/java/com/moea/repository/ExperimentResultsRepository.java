package com.moea.repository;

import com.moea.model.ExperimentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentResultsRepository extends JpaRepository<ExperimentResult, Long>, ExperimentResultsRepositoryCustom {

}

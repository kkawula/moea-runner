package com.moea.repository;

import com.moea.model.ExperimentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentResultsRepository extends JpaRepository<ExperimentResult, Long> {
    List<ExperimentResult> findByExperimentId(Long experiment_id);
}

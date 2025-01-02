package com.moea.repository;

import com.moea.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long>, ExperimentRepositoryCustom, JpaSpecificationExecutor<Experiment> {

}

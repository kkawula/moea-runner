package com.moea.repository;

import com.moea.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long>, ExperimentRepositoryCustom, JpaSpecificationExecutor<Experiment> {

}

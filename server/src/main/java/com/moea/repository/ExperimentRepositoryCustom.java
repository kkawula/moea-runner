package com.moea.repository;

import com.moea.model.Experiment;

import java.util.List;

public interface ExperimentRepositoryCustom {

    List<Experiment> findDistinctByInvocationId();

}

package com.moea.repository;

import com.moea.model.Experiment;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ExperimentRepositoryCustom {

    List<Experiment> findDistinctByGroupId();

}

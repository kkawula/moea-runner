package com.moea.service;

import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.repository.ExperimentRepository;
import org.springframework.stereotype.Service;

@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;

    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public int createAndRunExperiment(ExperimentDTO experiment) {
        Experiment result = experimentRepository.save(new Experiment(experiment.evaluations()));

        //TODO: Run experiment

        return result.getId();
    }
}

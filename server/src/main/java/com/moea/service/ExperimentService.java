package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
    }

    public Long createAndRunExperiment(ExperimentDTO experimentDto) {
        Experiment result = experimentRepository.save(new Experiment());

        //TODO: Implement experiment running logic

        return result.getId();
    }

    public List<Experiment> getExperiments() {
        return  experimentRepository.findAll();
    }

    public List<ExperimentMetricResult> getExperimentResults(int id) {
        return experimentResultsRepository.getResults(id);
    }

    public ExperimentStatus getExperimentStatus(int id) {
        return experimentRepository.findById(id).map(Experiment::getStatus).orElse(null);
    }
}

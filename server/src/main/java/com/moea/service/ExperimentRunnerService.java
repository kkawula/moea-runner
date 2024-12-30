package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetric;
import com.moea.model.ExperimentResult;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.util.ExperimentMapper;
import jakarta.transaction.Transactional;
import org.moeaframework.analysis.collector.Observation;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class ExperimentRunnerService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentMapper experimentMapper;

    public ExperimentRunnerService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentMapper experimentMapper) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentMapper = experimentMapper;
    }

    @Transactional
    public Long saveNewRunningExperiment(ExperimentDTO experimentDTO) {
        Experiment experiment = experimentMapper.fromDTO(experimentDTO);
        experiment.setStatus(ExperimentStatus.RUNNING);
        experiment.setStartDate(Calendar.getInstance().getTime());

        Experiment result = experimentRepository.save(experiment);
        return result.getId();
    }

    @Transactional
    public void saveExperimentResults(Long experimentId, List<AlgorithmProblemResult> results) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow(ExperimentNotFoundException::new);

        experiment.setEndDate(Calendar.getInstance().getTime());
        experimentRepository.save(experiment);

        List<String> metricsToSave = experiment.getMetrics().stream()
                .map(ExperimentMetric::getMetricName)
                .toList();

        for (AlgorithmProblemResult result : results) {
            for (Observation row : result.getObservations()) {
                for (String metric : metricsToSave) {
                    ExperimentResult experimentResult = ExperimentResult.builder()
                            .experiment(experiment)
                            .problem(result.getProblemName())
                            .algorithm(result.getAlgorithmName())
                            .metric(metric)
                            .iteration(row.getNFE())
                            .result((Double) row.get(metric))
                            .build();

                    experimentResultsRepository.save(experimentResult);
                }
            }
        }

        updateExperimentStatus(experiment, ExperimentStatus.FINISHED);
    }

    public void updateExperimentStatus(Experiment experiment, ExperimentStatus status) {
        experiment.setStatus(status);
        experimentRepository.save(experiment);
    }
}

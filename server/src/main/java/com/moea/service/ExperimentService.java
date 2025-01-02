package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.model.Problem;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import com.moea.util.ExperimentMapper;
import com.moea.util.ExperimentValidator;
import com.moea.util.ExperimentsResultsAggregator;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentRunnerService experimentRunnerService;
    private final ExperimentValidator experimentValidator;
    private final ExperimentMapper experimentMapper;
    private final ExperimentSpecifications experimentSpecifications;
    private final ExperimentsResultsAggregator experimentsResultsAggregator;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentRunnerService experimentRunnerService, ExperimentValidator experimentValidator, ExperimentMapper experimentMapper, ExperimentSpecifications experimentSpecifications, ExperimentsResultsAggregator experimentsResultsAggregator) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentRunnerService = experimentRunnerService;
        this.experimentValidator = experimentValidator;
        this.experimentMapper = experimentMapper;
        this.experimentSpecifications = experimentSpecifications;
        this.experimentsResultsAggregator = experimentsResultsAggregator;
    }

    public static Date convertStringToDate(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateString == null ? null : dateFormat.parse(dateString);
    }

    public Long createExperiment(ExperimentDTO experimentDTO) {
        experimentValidator.validate(experimentDTO);
        Long newExperimentID = experimentRunnerService.saveNewRunningExperiment(experimentDTO);
        List<AlgorithmProblemResult> results = new ArrayList<>();

        createAndRunExperiment(newExperimentID)
                .doOnNext(results::add)
                .observeOn(Schedulers.io())
                .doOnComplete(() -> experimentRunnerService.saveExperimentResults(newExperimentID, results))
                .doOnError(e -> updateExperimentStatus(newExperimentID, ExperimentStatus.ERROR))
                .subscribe();

        return newExperimentID;
    }

    private Observable<AlgorithmProblemResult> createAndRunExperiment(Long ExperimentId) {
        Experiment experiment = experimentRepository.findById(ExperimentId).orElseThrow(ExperimentNotFoundException::new);

        return Observable.<AlgorithmProblemResult>create(observer -> {
            try {
                for (Problem problem : experiment.getProblems()) {
                    Instrumenter instrumenter;
                    for (Algorithm algorithm : experiment.getAlgorithms()) {
                        if (observer.isDisposed()) {
                            break;
                        }

                        instrumenter = new Instrumenter()
                                .withProblem(problem.getProblemName())
                                .attachAllMetricCollectors();

                        new Executor()
                                .withProblem(problem.getProblemName())
                                .withAlgorithm(algorithm.getAlgorithmName())
                                .withMaxEvaluations(experiment.getEvaluations())
                                .withInstrumenter(instrumenter)
                                .run();

                        AlgorithmProblemResult result = AlgorithmProblemResult.builder()
                                .algorithmName(algorithm.getAlgorithmName())
                                .problemName(problem.getProblemName())
                                .observations(instrumenter.getObservations())
                                .build();

                        observer.onNext(result);
                    }
                }
            } catch (ProviderNotFoundException e) {
                observer.onError(e);
            }
            observer.onComplete();
        }).subscribeOn(Schedulers.computation());
    }

    public Long repeatExperiment(Long id) {
        Experiment experiment = experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        ExperimentDTO experimentDTO = experimentMapper.toRequestDTO(experiment);
        return createExperiment(experimentDTO);
    }

    public List<Experiment> getExperiments(String algorithmName, String problemName, String status, String metric, String fromDate, String toDate) throws ParseException {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withAlgorithm(algorithmName))
                .and(experimentSpecifications.withProblem(problemName))
                .and(experimentSpecifications.withStatus(status))
                .and(experimentSpecifications.withMetric(metric))
                .and(experimentSpecifications.withinDateRange(convertStringToDate(fromDate), convertStringToDate(toDate)));

        return experimentRepository.findAll(spec);
    }

    public List<Experiment> getUniqueExperiments() {
        return experimentRepository.findDistinctByGroupId();
    }

    public List<ExperimentResult> getExperimentResults(Long id) {
        experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        return experimentResultsRepository.findByExperimentId(id);
    }

    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResults(List<Long> experimentIds) {
        List<Experiment> experiments = experimentRepository.findAllById(experimentIds);

        if (experiments.size() != experimentIds.size()) {
            throw new ExperimentNotFoundException();
        }

        Map<Long, List<ExperimentResult>> experimentsResults = new HashMap<>();

        for (Experiment experiment : experiments) {
            List<ExperimentResult> experimentResults = experimentResultsRepository.findByExperimentId(experiment.getId());
            experimentsResults.put(experiment.getId(), experimentResults);
        }

        return experimentsResultsAggregator.combineResults(experiments, experimentsResults);
    }

    public ExperimentStatus getExperimentStatus(Long id) {
        return experimentRepository.findById(id).map(Experiment::getStatus)
                .orElseThrow(ExperimentNotFoundException::new);
    }

    public void updateExperimentStatus(Long experimentId, ExperimentStatus status) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow(ExperimentNotFoundException::new);
        experiment.setStatus(status);
        experimentRepository.save(experiment);
    }
}

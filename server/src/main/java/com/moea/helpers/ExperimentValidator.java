package com.moea.helpers;

import com.moea.dto.ExperimentDTO;
import org.moeaframework.core.indicator.StandardIndicator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ExperimentValidator {

    public void validate(ExperimentDTO experimentDTO) {
        validateEvaluations(experimentDTO);
        validateAlgorithms(experimentDTO);
        validateProblems(experimentDTO);
        validateMetrics(experimentDTO);
    }

    private void validateEvaluations(ExperimentDTO experimentDTO) {
        if (experimentDTO.getEvaluations() <= 0) {
            throw new IllegalArgumentException("Evaluations must be greater than 0");
        }
    }

    private void validateAlgorithms(ExperimentDTO experimentDTO) {
        if (experimentDTO.getAlgorithms().isEmpty()) {
            throw new IllegalArgumentException("At least one algorithm must be selected");
        }
    }

    private void validateProblems(ExperimentDTO experimentDTO) {
        if (experimentDTO.getProblems().isEmpty()) {
            throw new IllegalArgumentException("At least one problem must be selected");
        }
    }

    private void validateMetrics(ExperimentDTO experimentDTO) {
        if (experimentDTO.getMetrics().isEmpty()) {
            throw new IllegalArgumentException("At least one metric must be selected");
        }

        Set<String> validMetrics = Arrays.stream(StandardIndicator.values()).map(StandardIndicator::name).collect(Collectors.toSet());

        for (String metric : experimentDTO.getMetrics()) {
            if (!validMetrics.contains(metric)) {
                throw new IllegalArgumentException("Invalid metric: " + metric);
            }
        }
    }
}

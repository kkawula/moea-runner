package com.moea;

import com.moea.dto.ExperimentDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TestConst {
    private static final List<String> ALGORITHMS = List.of(
            "NSGAII",
            "GDE3",
            "IBEA",
            "PAES"
    );

    private static final List<String> PROBLEMS = List.of(
            "UF1",
            "LZ1",
            "WFG1",
            "ZDT1"
    );

    private static final List<String> METRICS = List.of(
            "R1Indicator",
            "Hypervolume",
            "Spacing",
            "R3Indicator"
    );

    private static final List<ExperimentDTO> EXPERIMENTS = List.of(
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(0, 2))
                    .problems(PROBLEMS.subList(0, 2))
                    .metrics(METRICS.subList(0, 2))
                    .startDate(createDate(2025, Calendar.JANUARY, 1, 1, 0, 0))
                    .endDate(createDate(2025, Calendar.JANUARY, 1, 2, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(2, 4))
                    .problems(PROBLEMS.subList(2, 4))
                    .metrics(METRICS.subList(2, 4))
                    .startDate(createDate(2025, Calendar.JANUARY, 1, 2, 0, 0))
                    .endDate(createDate(2025, Calendar.JANUARY, 1, 3, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(1, 2))
                    .problems(PROBLEMS.subList(1, 2))
                    .metrics(METRICS.subList(1, 2))
                    .startDate(createDate(2025, Calendar.JANUARY, 1, 4, 0, 0))
                    .endDate(createDate(2025, Calendar.JANUARY, 1, 5, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(1, 4))
                    .problems(PROBLEMS.subList(1, 4))
                    .metrics(METRICS.subList(1, 4))
                    .startDate(createDate(2025, Calendar.JANUARY, 2, 1, 0, 0))
                    .endDate(createDate(2025, Calendar.JANUARY, 2, 2, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(3, 4))
                    .problems(PROBLEMS.subList(3, 4))
                    .metrics(METRICS.subList(3, 4))
                    .startDate(createDate(2025, Calendar.JANUARY, 3, 1, 0, 0))
                    .endDate(createDate(2025, Calendar.JANUARY, 3, 2, 0, 0))
                    .build()
    );

    public static List<String> getAlgorithms() {
        return ALGORITHMS;
    }

    public static List<String> getProblems() {
        return PROBLEMS;
    }

    public static List<String> getMetrics() {
        return METRICS;
    }

    public static List<ExperimentDTO> getExperiments() {
        return EXPERIMENTS;
    }

    private static Date createDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTime();
    }
}
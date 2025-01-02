package com.moea.dto;

import java.util.List;

public record ExperimentsCommonAttributes(int iterations, List<String> problemNames, List<String> algorithmNames,
                                          List<String> metricNames) {
}

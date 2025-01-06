package com.moea.specifications;

import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.Problem;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExperimentSpecifications {

    public Specification<Experiment> withExperimentIds(List<Long> experimentsId) {
        return (root, query, criteriaBuilder) -> {
            if (experimentsId == null || experimentsId.isEmpty()) {
                return null;
            }
            Path<Long> id = root.get("id");
            return id.in(experimentsId);
        };
    }

    public Specification<Experiment> withAlgorithm(String algorithmName) {
        return (root, query, criteriaBuilder) -> {
            if (algorithmName == null) {
                return null;
            }
            Join<Experiment, Algorithm> algorithmJoin = root.join("algorithms");
            return criteriaBuilder.equal(algorithmJoin.get("algorithmName"), algorithmName);
        };
    }

    public Specification<Experiment> withProblem(String problemName) {
        return (root, query, criteriaBuilder) -> {
            if (problemName == null) {
                return null;
            }
            Join<Experiment, Problem> problemJoin = root.join("problems", JoinType.LEFT);
            return criteriaBuilder.equal(problemJoin.get("problemName"), problemName);
        };
    }

    public Specification<Experiment> withMetric(String metricName) {
        return (root, query, criteriaBuilder) -> {
            if (metricName == null) {
                return null;
            }
            Join<Experiment, Problem> problemJoin = root.join("metrics");
            return criteriaBuilder.equal(problemJoin.get("metricName"), metricName);
        };
    }

    public Specification<Experiment> withStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public Specification<Experiment> withinDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null & toDate == null) {
                return null;
            } else if (fromDate != null & toDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), fromDate);
            } else if (fromDate != null) {
                return criteriaBuilder.between(root.get("startDate"), fromDate, toDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), toDate);
            }
        };
    }

}

package com.moea.specifications;

import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.Problem;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ExperimentSpecifications {

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
            Join<Experiment, Problem> problemJoin = root.join("problems");
            return criteriaBuilder.equal(problemJoin.get("problemName"), problemName);
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

    public Specification<Experiment> withinDateRange(Date fromDate, Date toDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null || toDate == null) {
                return null;
            }
            return criteriaBuilder.between(root.get("startDate"), fromDate, toDate);
        };
    }

}

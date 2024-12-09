package com.moea.repository;

import com.moea.model.ExperimentMetricResult;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExperimentResultsRepositoryImpl implements ExperimentResultsRepositoryCustom {
    EntityManager entityManager;

    public ExperimentResultsRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ExperimentMetricResult> getResults(String experimentId) {
        return entityManager.createQuery("SELECT e FROM ExperimentMetricResult e WHERE e.experiment.id = :experimentId", ExperimentMetricResult.class)
                .setParameter("experimentId", experimentId)
                .getResultList();
    }
}

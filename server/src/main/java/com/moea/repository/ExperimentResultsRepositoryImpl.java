package com.moea.repository;

import com.moea.model.ExperimentResult;
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
    public List<ExperimentResult> getResults(String experimentId) {
        return entityManager.createQuery("SELECT e FROM ExperimentResult e WHERE e.experiment.id = :experimentId", ExperimentResult.class)
                .setParameter("experimentId", experimentId)
                .getResultList();
    }
}

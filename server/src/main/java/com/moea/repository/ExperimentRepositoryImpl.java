package com.moea.repository;

import com.moea.model.Experiment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExperimentRepositoryImpl implements ExperimentRepositoryCustom {

    private final EntityManager entityManager;

    public ExperimentRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Experiment> findDistinctByInvocationId() {
        String jpql = """
                    SELECT e FROM Experiment e WHERE e.id IN (
                                SELECT MIN(e.id) FROM Experiment e GROUP BY e.invocationId
                                )
                """;

        TypedQuery<Experiment> query = entityManager.createQuery(jpql, Experiment.class);

        return query.getResultList();
    }
}

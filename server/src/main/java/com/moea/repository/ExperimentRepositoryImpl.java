package com.moea.repository;

import com.moea.model.Experiment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class ExperimentRepositoryImpl implements ExperimentRepositoryCustom {

    private final EntityManager entityManager;

    public ExperimentRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Experiment> findDistinctByGroupId() {
        String jpql = """
                    SELECT e FROM Experiment e WHERE e.id IN (
                                SELECT MIN(e.id) FROM Experiment e GROUP BY e.groupId
                                )
                """;

        TypedQuery<Experiment> query = entityManager.createQuery(jpql, Experiment.class);

        return query.getResultList();
    }
}

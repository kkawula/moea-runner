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
    public List<Experiment> findDistinctByGroupId(Collection<UUID> groupIds) {
        String jpql = """
                    SELECT e FROM Experiment e WHERE e.id IN (
                        SELECT MIN(e2.id) FROM Experiment e2 WHERE e2.groupId IN :groupIds GROUP BY e2.groupId
                    )
                """;

        TypedQuery<Experiment> query = entityManager.createQuery(jpql, Experiment.class);
        query.setParameter("groupIds", groupIds);

        return query.getResultList();
    }

}

package com.moea.repository;

import com.moea.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    @Query("SELECT e FROM Experiment e WHERE e.id IN (" +
            "SELECT MIN(e.id) FROM Experiment e GROUP BY e.groupId" +
            ")")
    List<Experiment> findDistinctByGroupId(Collection<UUID> groupIds);
}

package com.moea.repository;

import com.moea.TestConst;
import com.moea.model.Experiment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ExperimentRepositoryTest {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void testDeleteByGroupName() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            // Given
            String groupName1 = "testGroup1";
            String groupName2 = "testGroup2";
            List<Experiment> experiments = TestConst.getAggregatedExperiments();
            experiments.get(0).setGroupName(groupName1);
            experiments.get(1).setGroupName(groupName2);
            experiments.get(2).setGroupName(groupName2);
            experimentRepository.saveAll(experiments);

            // When
            experimentRepository.deleteByGroupName("testGroup2");

            // Then
            List<Experiment> remainingExperiments = experimentRepository.findAll();
            assertEquals(1, remainingExperiments.size());
            assertEquals("testGroup1", remainingExperiments.getFirst().getGroupName());
        });
    }
}
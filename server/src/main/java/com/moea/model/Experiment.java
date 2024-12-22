package com.moea.model;

import com.moea.ExperimentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@Table(name = "experiments")
@AllArgsConstructor
@NoArgsConstructor
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "evaluations")
    private int evaluations;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExperimentStatus status;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @OneToMany(
            mappedBy = "experiment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Algorithm> algorithms = new ArrayList<>();

    @OneToMany(
            mappedBy = "experiment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Problem> problems = new ArrayList<>();

    @OneToMany(
            mappedBy = "experiment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ExperimentMetric> metrics = new ArrayList<>();

    @OneToMany(
            mappedBy = "experiment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ExperimentResult> experimentResults = new ArrayList<>();
}

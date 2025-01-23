package com.moea.model;

import com.moea.ExperimentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder(toBuilder = true)
@Table(name = "experiments")
@AllArgsConstructor
@NoArgsConstructor
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invocation_id")
    private UUID invocationId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "evaluations")
    private int evaluations;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExperimentStatus status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

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

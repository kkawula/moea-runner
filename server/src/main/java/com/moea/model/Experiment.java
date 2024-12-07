package com.moea.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@ToString
@Table(name = "experiments")
@AllArgsConstructor
@NoArgsConstructor
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluations")
    private int evaluations;

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
    private List<ExperimentMetricResult> experimentMetricResults = new ArrayList<>();

}

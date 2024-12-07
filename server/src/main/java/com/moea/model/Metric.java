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
@Table(name = "metrics")
@AllArgsConstructor
@NoArgsConstructor
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(
            mappedBy = "metric",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ExperimentMetricResult> experimentMetricResults = new ArrayList<>();
}

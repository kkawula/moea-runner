package com.moea.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@Table(name = "algorithms")
@AllArgsConstructor
@NoArgsConstructor
public class Algorithm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Experiment experiment;

    @Column(name = "algorithm_name")
    private String algorithmName;

}

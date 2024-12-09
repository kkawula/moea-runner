package com.moea.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@ToString
@Table(name = "algorithms")
@AllArgsConstructor
@NoArgsConstructor
public class Algorithm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Experiment experiment;

    @Column(name = "algorithm_name")
    private String algorithmName;

}

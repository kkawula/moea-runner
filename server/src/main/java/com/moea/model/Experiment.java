package com.moea.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int evaluations;

    public Experiment() {}

    public Experiment(int evaluations) {
        this.evaluations = evaluations;
    }

    public int getId() {
        return id;
    }

    public int getEvaluations() {
        return evaluations;
    }
}

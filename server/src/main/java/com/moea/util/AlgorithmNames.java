package com.moea.util;

public enum AlgorithmNames {
    NSGAII("NSGAII"),
    GDE3("GDE3");

    //TODO: Add the rest of the algorithms

    private final String algorithmName;

    AlgorithmNames(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    @Override
    public String toString() {
        return algorithmName;
    }
}

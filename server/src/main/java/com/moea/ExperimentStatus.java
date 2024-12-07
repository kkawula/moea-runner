package com.moea;

public enum ExperimentStatus {
    RUNNING("running"),
    FINISHED("finished"),
    ERROR("error");

    private final String status;

    ExperimentStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}

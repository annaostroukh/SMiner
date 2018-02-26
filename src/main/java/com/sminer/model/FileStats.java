package com.sminer.model;

public class FileStats {
    private long totalAmountOfTrajectories;
    private long validTrajectories;
    private double elapsedTime;

    public long getTotalAmountOfTrajectories() {
        return totalAmountOfTrajectories;
    }

    public FileStats setTotalAmountOfTrajectories(long totalAmountOfTrajectories) {
        this.totalAmountOfTrajectories = totalAmountOfTrajectories;
        return this;
    }

    public long getValidTrajectories() {
        return validTrajectories;
    }

    public FileStats setValidTrajectories(long validTrajectories) {
        this.validTrajectories = validTrajectories;
        return this;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public FileStats setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }
}

package com.sminer.model;

public class FileStats {
    private long totalAmountOfRecords;
    private long validRecords;
    private double elapsedTime;

    public long getTotalAmountOfRecords() {
        return totalAmountOfRecords;
    }

    public FileStats setTotalAmountOfRecords(long totalAmountOfRecords) {
        this.totalAmountOfRecords = totalAmountOfRecords;
        return this;
    }

    public long getValidRecords() {
        return validRecords;
    }

    public FileStats setValidRecords(long validRecords) {
        this.validRecords = validRecords;
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

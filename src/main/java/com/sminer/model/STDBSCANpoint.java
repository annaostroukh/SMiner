package com.sminer.model;

import java.sql.Timestamp;

public class STDBSCANpoint extends Record {
    private boolean isNoise = false;
    private boolean isProcessed = false;
    private Integer clusterId = null;

    public STDBSCANpoint(int modId, Timestamp startTime, double longitude, double lattitude) {
        super(modId, startTime, longitude, lattitude);
    }

    public boolean isNoise() {
        return isNoise;
    }

    public void setNoise(boolean noise) {
        isNoise = noise;
    }

    public boolean isProcessed() { return isProcessed; }

    public void setProcessed(boolean processed) { isProcessed = processed; }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
}

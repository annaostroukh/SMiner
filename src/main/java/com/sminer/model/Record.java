package com.sminer.model;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * Entity for storing GPS record data
 */
public class Record {
    private final int modId;
    private final Timestamp timestamp;
    private Duration stopDuration;
    private boolean stop;
    private int stopId;
    private final double longitude;
    private final double lattitude;

    public Record(int modId, Timestamp startTime, double longitude, double lattitude) {
        this.modId = modId;
        this.timestamp = startTime;
        this.longitude = longitude;
        this.lattitude = lattitude;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getModId() {
        return modId;
    }

    public Duration getStopDuration() {
        return stopDuration;
    }

    public void setStopDuration(Duration stopDuration) {
        this.stopDuration = stopDuration;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLattitude() {
        return lattitude;
    }


    public String toString() {
        return "ModId: '" + this.modId
                + "', StartTime: '" + this.timestamp + "'";
    }

}

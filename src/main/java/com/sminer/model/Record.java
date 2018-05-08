package com.sminer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

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

    @JsonIgnore
    public Duration getStopDuration() {
        return stopDuration;
    }

    public void setStopDuration(Duration stopDuration) {
        this.stopDuration = stopDuration;
    }

    @JsonIgnore
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

    public long getFormattedDurationInMin() { return stopDuration.toMinutes();}

    public String getFormattedPoint() { return this.toString(); }

    public String getFormattedDateTime() { return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(this.timestamp.getTime()));}

    public String toString() {
        return "ModId: " + this.modId
                + ", Time: " + this.timestamp
                + ", Longitude : " + this.longitude
                + ", Lattitude : " + this.lattitude;
    }

}

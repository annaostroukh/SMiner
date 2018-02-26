package com.sminer.model;

import java.sql.Timestamp;

/**
 * Entity for storing trajectory data
 */
public class Trajectory {
    private final int modId;
    private final int tripId;
    private final Timestamp startTime;
    private final Timestamp endTime;
    private final double xStart;
    private final double xEnd;
    private final double yStart;
    private final double yEnd;
    private double longitude;
    private double lattitude;

    public Trajectory(int modId, int tripId, Timestamp startTime, Timestamp endTime, double xStart,
                      double xEnd, double yStart, double yEnd) {
        this.modId = modId;
        this.tripId = tripId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.xStart = xStart;
        this.xEnd = xEnd;
        this.yStart = yStart;
        this.yEnd = yEnd;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public double getxStart() {
        return xStart;
    }

    public double getxEnd() {
        return xEnd;
    }

    public double getyStart() {
        return yStart;
    }

    public double getyEnd() {
        return yEnd;
    }

    public int getModId() {
        return modId;
    }

    public int getTripId() {
        return tripId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public String toString() {
        return "ModId: '" + this.modId
                + "', TripId: '" + this.tripId
                + "', StartTime: '" + this.startTime + "'"
                + "', EndTime: '" + this.endTime + "'";
    }

}

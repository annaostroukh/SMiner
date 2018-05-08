package com.sminer.model;

import java.util.List;

public class ModCluster {
    private final int clusterId;
    private final double lattitudeMin;
    private final double lattitudeMax;
    private final double longitudeMin;
    private final double longitudeMax;
    private double clusterCentroidLat;
    private double clusterCentroidLon;
    private final List<Record> points;

    public ModCluster(int clusterId, double lattitudeMin, double lattitudeMax, double longitudeMin, double longitudeMax,
                      List<Record> points) {
        this.clusterId = clusterId;
        this.lattitudeMin = lattitudeMin;
        this.lattitudeMax = lattitudeMax;
        this.longitudeMin = longitudeMin;
        this.longitudeMax = longitudeMax;
        this.points = points;
    }

    public double getClusterCentroidLat() {
        return clusterCentroidLat;
    }

    public void setClusterCentroidLat(double clusterCentroidLat) {
        this.clusterCentroidLat = clusterCentroidLat;
    }

    public double getClusterCentroidLon() { return clusterCentroidLon; }

    public void setClusterCentroidLon(double clusterCentroidLon) { this.clusterCentroidLon = clusterCentroidLon; }

    public int getClusterId() {
        return clusterId;
    }

    public double getLattitudeMin() {
        return lattitudeMin;
    }

    public double getLattitudeMax() {
        return lattitudeMax;
    }

    public double getLongitudeMin() {
        return longitudeMin;
    }

    public double getLongitudeMax() {
        return longitudeMax;
    }

    public List<Record> getPoints() { return points; }
}

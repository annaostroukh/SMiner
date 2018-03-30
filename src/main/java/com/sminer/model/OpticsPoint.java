package com.sminer.model;

import java.sql.Timestamp;

public class OpticsPoint<T extends Comparable<T>> extends Record implements Comparable<OpticsPoint<T>> {
    private T reachabilityDistance = null;
    private T coreDistance = null;
    private int order;
    private boolean isProcessed = false;

    public OpticsPoint(int modId, Timestamp startTime, double longitude, double lattitude) {
        super(modId, startTime, longitude, lattitude);
    }

    public T getReachabilityDistance() {
        return reachabilityDistance;
    }

    public void setReachabilityDistance(T reachabilityDistance) {
        this.reachabilityDistance = reachabilityDistance;
    }

    public T getCoreDistance() {
        return coreDistance;
    }

    public void setCoreDistance(T coreDistance) {
        this.coreDistance = coreDistance;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(OpticsPoint<T> o) {
        return getCoreDistance().compareTo(o.getCoreDistance());
    }
}

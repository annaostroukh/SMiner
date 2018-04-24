package com.sminer.model;

public class SpatialTemporalDim {
    private final Double spatialDim;
    private final Integer temporalDim;

    public SpatialTemporalDim(Integer temporalDim, Double spatialDim) {
        this.spatialDim = spatialDim;
        this.temporalDim = temporalDim;
    }

    public Double getSpatialDim() {
        return spatialDim;
    }

    public Integer getTemporalDim() {
        return temporalDim;
    }
}

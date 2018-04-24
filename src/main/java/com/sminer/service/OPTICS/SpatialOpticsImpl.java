package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.SpatialTemporalDim;
import org.springframework.stereotype.Component;

@Component
public class SpatialOpticsImpl extends AbstractOpticsImpl<Double> {
    private static final double DEGREE_LENGTH = 110.25;

    @Override
    protected Double getDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        double deltaX = pointP.getLattitude() - pointQ.getLattitude();
        double deltaY = (pointP.getLongitude() - pointQ.getLongitude()) * Math.cos(pointQ.getLattitude());
        return DEGREE_LENGTH * Math.sqrt((Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
    }

    @Override
    protected SpatialTemporalDim getSpatialTemporalDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return null;
    }
}

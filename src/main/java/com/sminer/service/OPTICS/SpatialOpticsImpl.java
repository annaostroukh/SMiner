package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Component
public class SpatialOpticsImpl extends AbstractOpticsImpl<Double, Double> {
    private static final double DEGREE_LENGTH = 110.25;

    @Override
    protected Double getCoreDistance(Set<OpticsPoint> points, OpticsPoint point) {
        return points.stream()
                .map(neighbour -> getDistance(point, neighbour))
                .min(Double::compareTo).orElseThrow(NoSuchElementException::new);
    }

    @Override
    protected Double getDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        double deltaX = pointP.getLattitude() - pointQ.getLattitude();
        double deltaY = (pointP.getLongitude() - pointQ.getLongitude()) * Math.cos(pointQ.getLattitude());
        return DEGREE_LENGTH * Math.sqrt((Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
    }
}

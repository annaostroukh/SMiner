package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.SpatialTemporalDim;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TemporalOpticsImpl extends AbstractOpticsImpl<Integer, Integer> {
    @Override
    protected Integer getDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        //return (int) Math.sqrt(Math.pow(Duration.between(pointP.getTimestamp().toInstant(), pointQ.getTimestamp().toInstant()).toMinutes(), 2));
        return Math.toIntExact(Math.abs(Duration.between(pointP.getTimestamp().toInstant(), pointQ.getTimestamp().toInstant()).toMinutes()));
    }

    @Override
    protected Integer getCoreDistance(Set<OpticsPoint> points, OpticsPoint point) {
        return points.stream()
                .map(neighbour -> getDistance(point, neighbour))
                .min(Integer::compareTo).orElseThrow(NoSuchElementException::new);
    }
}

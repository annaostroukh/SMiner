package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.SpatialTemporalDim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SpatialTemporalOpticsImpl extends AbstractOpticsImpl<Integer, SpatialTemporalDim> {

    @Autowired
    private SpatialOpticsImpl spatialOptics;
    @Autowired
    private TemporalOpticsImpl temporalOptics;

    @Override
    protected boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, Integer epsilonTemporal, Double epsilonSpatial) {
        return spatialOptics.getDistance(pointP, pointQ).compareTo(epsilonSpatial) <=0 &&
                temporalOptics.getDistance(pointP, pointQ).compareTo(epsilonTemporal) <= 0;
    }

    @Override
    protected SpatialTemporalDim getSpatialTemporalDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return new SpatialTemporalDim(temporalOptics.getDistance(pointP, pointQ), spatialOptics.getDistance(pointP, pointQ));
    }

    @Override
    protected void asSortedList(List<OpticsPoint> list) {
        list.sort(Comparator.comparing((OpticsPoint e) -> e.getReachabilityDistanceTwoDim().getTemporalDim())
                .thenComparing((OpticsPoint e) -> e.getReachabilityDistanceTwoDim().getSpatialDim()));
    }

    @Override
    protected Integer getDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return null;
    }

    @Override
    protected SpatialTemporalDim getCoreDistance(Set<OpticsPoint> points, OpticsPoint point) {
        return points.stream()
                .map(neighbour -> getSpatialTemporalDistance(point, neighbour))
                .min(Comparator.comparing(SpatialTemporalDim::getTemporalDim).thenComparing(SpatialTemporalDim::getSpatialDim))
                .orElseThrow(NoSuchElementException::new);
    }
}

package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.SpatialTemporalDim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpatialTemporalOpticsImpl extends AbstractOpticsImpl<Integer> {

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
}

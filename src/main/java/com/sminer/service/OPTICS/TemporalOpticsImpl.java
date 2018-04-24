package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.SpatialTemporalDim;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TemporalOpticsImpl extends AbstractOpticsImpl<Integer> {
    @Override
    protected Integer getDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return (int) Math.sqrt(Math.pow(Duration.between(pointP.getTimestamp().toInstant(), pointQ.getTimestamp().toInstant()).toMinutes(), 2));
    }

    @Override
    protected SpatialTemporalDim getSpatialTemporalDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return null;
    }
}

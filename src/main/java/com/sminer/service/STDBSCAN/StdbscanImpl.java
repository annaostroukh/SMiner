package com.sminer.service.STDBSCAN;

import com.sminer.model.Record;
import com.sminer.model.StdbscanPoint;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class StdbscanImpl implements IStdbscan {
    private static final double DEGREE_LENGTH = 110.25;
    /**
     * Function converting Records to STDBSCAN data structure and grouping them by moving object Id
     */
    private static final Function<List<Record>, Map<Integer, List<StdbscanPoint>>> recordsToSTDBSCANPointsById = (records ->
            records.stream()
                    .map(record -> {
                        StdbscanPoint point = new StdbscanPoint(record.getModId(), record.getTimestamp(), record.getLongitude(), record.getLattitude());
                        point.setStopDuration(record.getStopDuration());
                        point.setStopId(record.getStopId());
                        return point;
                    })
                    .collect(groupingBy(StdbscanPoint::getModId))
    );
    /**
     * Valid point is a point which does not belong to a cluster and isn't marked as noise
     */
    private static final Predicate<StdbscanPoint> isValid = (StdbscanPoint point) -> point.getClusterId() == null
            && !point.isNoise();

    @Override
    public List<StdbscanPoint> runSTDBSCAN(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts) {
        int clusterId = 0;
        Set<StdbscanPoint> neighbours = new HashSet<>();
        Map<Integer, List<StdbscanPoint>> points = recordsToSTDBSCANPointsById.apply(records);

        for (Map.Entry<Integer, List<StdbscanPoint>> entry : points.entrySet()) {
            for (StdbscanPoint point: entry.getValue().stream().filter(isValid).collect(Collectors.toList())) {
                neighbours.clear();
                neighbours.addAll(expandCluster(point, points, epsilonTemporal, epsilonSpatial, new ArrayList<>(), minPts, clusterId));
                if (!neighbours.isEmpty()) {
                    List<StdbscanPoint> seedList = new ArrayList<>(neighbours);
                    while (!seedList.isEmpty()) {
                        StdbscanPoint current = seedList.get(0);
                        seedList = expandCluster(current, points, epsilonTemporal, epsilonSpatial, seedList, minPts, clusterId);
                    }
                    clusterId++;
                }
            }
        }
        return points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private List<StdbscanPoint> expandCluster(StdbscanPoint point, Map<Integer, List<StdbscanPoint>> database, Integer epsilonTemporal,
                                             Double epsilonSpatial, List<StdbscanPoint> seedList, int minPts, int clusterId) {
        // skip trajectory to which current data point belongs
        Map<Integer, List<StdbscanPoint>> data = database.entrySet().stream()
                .filter(entry -> entry.getKey() != point.getModId())
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));
        Set<StdbscanPoint> neighbours = new HashSet<>();
        data.entrySet().stream().forEach(trajectory -> {
            neighbours.addAll(getNeighbours(trajectory.getValue().stream().filter(isValid).collect(Collectors.toList()),
                    point, epsilonTemporal, epsilonSpatial));
        });

        if (neighbours.size() >= minPts) {
            neighbours.stream().forEach(neighbour -> neighbour.setClusterId(clusterId));
            neighbours.remove(point);
            neighbours.stream().filter(elem -> !seedList.contains(elem) && !elem.isProcessed()).forEach(elem -> seedList.add(elem));
        } else {
            if (point.getClusterId() == null) {
                point.setNoise(true);
            }
        }
        seedList.remove(point);
        point.setProcessed(true);
        return seedList;
    }

    private Set<StdbscanPoint> getNeighbours(List<StdbscanPoint> points, StdbscanPoint point, Integer epsilonTemporal, Double epsilonSpatial) {
        Set<StdbscanPoint> neighbours = points.stream().filter(record -> isNeighbour(point, record, epsilonTemporal, epsilonSpatial)).collect(Collectors.toSet());
        neighbours.add(point);
        return neighbours.stream().filter(p -> !p.isProcessed()).collect(Collectors.toSet());
    }

    private boolean isNeighbour(StdbscanPoint pointP, StdbscanPoint pointQ, Integer epsilonTemporal, Double epsilonSpatial) {
        return getTemporalDistance(pointP, pointQ) <= epsilonTemporal &&
                getSpatialDistance(pointP, pointQ) <= epsilonSpatial;
    }

    private Double getSpatialDistance(StdbscanPoint pointP, StdbscanPoint pointQ) {
        double deltaX = pointP.getLattitude()  - pointQ.getLattitude();
        double deltaY = (pointP.getLongitude() - pointQ.getLongitude()) * Math.cos(pointQ.getLattitude());
        return DEGREE_LENGTH * Math.sqrt((Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
    }

    private Integer getTemporalDistance(StdbscanPoint pointP, StdbscanPoint pointQ) {
        return (int) Math.sqrt(Math.pow(Duration.between(pointP.getTimestamp().toInstant(), pointQ.getTimestamp().toInstant()).toMinutes(), 2));
    }
}

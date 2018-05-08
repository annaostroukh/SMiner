package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.Record;
import com.sminer.model.SpatialTemporalDim;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public abstract class AbstractOpticsImpl<T extends Comparable<T>, D> implements IOptics<T> {
    /**
     * Function converting Records to Optics data structure and grouping them by moving object Id
     */
    private static final Function<List<Record>, Map<Integer, List<OpticsPoint>>> recordsToOpticsPointsById = (records ->
            records.stream()
                    .map(record -> new OpticsPoint(record.getModId(), record.getTimestamp(), record.getLongitude(), record.getLattitude()))
                    .collect(groupingBy(OpticsPoint::getModId))
    );
    private static final Predicate<OpticsPoint> notProcessed = (OpticsPoint point) -> !point.isProcessed();

    @Override
    public List<OpticsPoint> runOneDimensionOptics(List<Record> records, T epsilon, int minPts) {
        return runOptics(records, epsilon, null, minPts);
    }

    @Override
    public List<OpticsPoint> runSpatialTemporalOptics(List<Record> records, T epsilonTemporal, Double epsilonSpatial, int minPts) {
        return runOptics(records, epsilonTemporal, epsilonSpatial, minPts);
    }

    private List<OpticsPoint> runOptics(List<Record> records, T epsilon, Double epsilonSpatial, int minPts) {
        int order = 0;
        Set<OpticsPoint> neighbours = new HashSet<>();
        Map<Integer, List<OpticsPoint>> points = recordsToOpticsPointsById.apply(records);

        for (Map.Entry<Integer, List<OpticsPoint>> entry : points.entrySet()) {
            for (OpticsPoint point : entry.getValue().stream().filter(notProcessed).collect(Collectors.toList())) {
                neighbours.clear();
                neighbours.addAll(expandCluster(point, points, epsilon, epsilonSpatial, new ArrayList<>(), minPts, order));
                order++;
                if (!neighbours.isEmpty()) {
                    List<OpticsPoint> seedList = new ArrayList<>(neighbours);
                    asSortedList(seedList);
                    while (!seedList.isEmpty()) {
                        asSortedList(seedList);
                        OpticsPoint current = seedList.get(0);
                        seedList = expandCluster(current, points, epsilon, epsilonSpatial, seedList, minPts, order);
                        order++;
                    }
                }
            }
        }

        return points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private List<OpticsPoint> expandCluster(OpticsPoint point, Map<Integer, List<OpticsPoint>> database, T epsilon, Double epsilonSpatial, List<OpticsPoint> seedList, int minPts, int order) {
        Map<Integer, List<OpticsPoint>> data = database.entrySet().stream()
                .filter(entry -> entry.getKey() != point.getModId())
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));
        Set<OpticsPoint> neighbours = new HashSet<>();
        data.entrySet().stream().forEach(trajectory -> {
             neighbours.addAll(getNeighbours(trajectory.getValue().stream().filter(notProcessed).collect(Collectors.toList()), point, epsilon, epsilonSpatial));
        });

        seedList.remove(point);
        if (neighbours.size() >= minPts) {
            // remove core object from its neighbours list
            neighbours.remove(point);
            assignReachabilityDistance(point, neighbours, epsilonSpatial);
            neighbours.stream().filter(elem -> !seedList.contains(elem) && !elem.isProcessed()).forEach(elem -> seedList.add(elem));
        }

        point.setProcessed(true);
        point.setOrder(order);

        return seedList.stream().distinct().filter(notProcessed).collect(Collectors.toList());
    }

    private void assignReachabilityDistance(OpticsPoint point, Set<OpticsPoint> neighbours, Double epsilonSpatial) {
        D coreDistanceTwoDim;
        T coreDistanceOneDim;
        if (epsilonSpatial != null) {
            coreDistanceTwoDim = getCoreDistance(neighbours, point);
            // if to core object hasn't been assigned reachability distance, assign its core distance
            if (coreDistanceTwoDim != null && point.getReachabilityDistanceTwoDim() == null) {
                point.setReachabilityDistanceTwoDim((SpatialTemporalDim) coreDistanceTwoDim);
            }
            neighbours.forEach(neighbour -> neighbour.setReachabilityDistanceTwoDim(getSpatialTemporalDistance(point, neighbour)));
        } else {
            coreDistanceOneDim = (T) getCoreDistance(neighbours, point);
            if (coreDistanceOneDim != null && point.getReachabilityDistance() == null) {
                point.setReachabilityDistance(coreDistanceOneDim);
            }
            neighbours.forEach(neighbour -> neighbour.setReachabilityDistance(getDistance(point, neighbour)));
        }
    }

    private boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, T epsilon) {
        return getDistance(pointP, pointQ).compareTo(epsilon) <= 0;
    }

    protected abstract T getDistance(OpticsPoint pointP, OpticsPoint pointQ);

    protected abstract D getCoreDistance(Set<OpticsPoint> points, OpticsPoint point);

    protected SpatialTemporalDim getSpatialTemporalDistance(OpticsPoint pointP, OpticsPoint pointQ) { return null;}

    private Set<OpticsPoint> getNeighbours(List<OpticsPoint> points, OpticsPoint point, T epsilonOneDim, Double epsilonSpatial) {
        Set<OpticsPoint> neighbours = new HashSet<>();
        if (epsilonSpatial != null){
            neighbours.addAll(points.stream().filter(record -> isNeighbour(point, record, epsilonOneDim, epsilonSpatial)).collect(Collectors.toSet()));
        } else {
            neighbours.addAll(points.stream().filter(record -> isNeighbour(point, record, epsilonOneDim)).collect(Collectors.toSet()));
        }
        neighbours.add(point);
        return neighbours.stream().filter(notProcessed).collect(Collectors.toSet());
    }

    protected boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, T epsilonTemporal, Double epsilonSpatial) {
        return isNeighbour(pointP, pointQ, epsilonTemporal);
    }

    protected void asSortedList(List<OpticsPoint> list) {
        list.sort(Comparator.comparing(e -> e.getReachabilityDistance()));
    }
}

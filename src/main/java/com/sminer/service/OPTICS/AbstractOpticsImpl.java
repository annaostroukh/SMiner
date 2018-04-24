package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.Record;
import com.sminer.model.SpatialTemporalDim;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public abstract class AbstractOpticsImpl<T extends Comparable<T>> implements IOptics<T> {
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
    public List<OpticsPoint> runOneDimensionOPTICS(List<Record> records, T epsilon, int minPts) {
        return runOPTICS(records, epsilon, null, minPts);
    }

    @Override
    public List<OpticsPoint> runSpatialTemporalOPTICS(List<Record> records, T epsilonTemporal, Double epsilonSpatial, int minPts) {
        return runOPTICS(records, epsilonTemporal, epsilonSpatial, minPts);
    }

    private List<OpticsPoint> runOPTICS(List<Record> records, T epsilon, Double epsilonSpatial, int minPts) {
        int order = 0;
        List<OpticsPoint> seedList = new ArrayList<>();
        Map<Integer, List<OpticsPoint>> points = recordsToOpticsPointsById.apply(records);

        for (Map.Entry<Integer, List<OpticsPoint>> entry : points.entrySet()) {
            for (OpticsPoint point : entry.getValue().stream().filter(notProcessed).collect(Collectors.toList())) {
                seedList.clear();
                seedList.addAll(epsilonSpatial == null
                        ? expandCluster(point, points, epsilon, minPts, new ArrayList<>(), order)
                        : expandCluster(point, points, epsilon, epsilonSpatial, minPts, new ArrayList<>(), order));
                order++;
                if (!seedList.isEmpty()) {
                    asSortedList(seedList);
                    Map<Integer, List<OpticsPoint>> map = extractSeeds(seedList, points, epsilon, epsilonSpatial, minPts, order);
                    if (map.values().contains(Collections.emptyList())) {
                        order = map.entrySet().stream().map(e -> e.getKey()).findFirst().get();
                    }
                }
            }
        }
        return points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private Map<Integer, List<OpticsPoint>> extractSeeds(List<OpticsPoint> seedList, Map<Integer, List<OpticsPoint>> database, T epsilon,  Double epsilonSpatial, int minPts, int order) {
        if (seedList.stream().distinct().filter(notProcessed).collect(Collectors.toList()).isEmpty()) {
            Map<Integer, List<OpticsPoint>> map = new HashMap<>();
            map.put(order, Collections.emptyList());
            return map;
        }
        OpticsPoint seedPoint = seedList.stream().distinct().filter(notProcessed).collect(Collectors.toList()).get(0);
        seedList.addAll(epsilonSpatial == null
                    ? expandCluster(seedPoint, database, epsilon, minPts, seedList, order)
                    : expandCluster(seedPoint, database, epsilon, epsilonSpatial, minPts, seedList, order));
        order++;
        asSortedList(seedList);
        return extractSeeds(seedList, database, epsilon, epsilonSpatial, minPts, order);
    }

    private List<OpticsPoint> expandCluster(OpticsPoint point, Map<Integer, List<OpticsPoint>> database, T epsilon, int minPts, List<OpticsPoint> seedList, int order) {
        // skip trajectory to which current data point belongs
        Map<Integer, List<OpticsPoint>> data = database.entrySet().stream()
                .filter(entry -> entry.getKey() != point.getModId())
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));
        Set<OpticsPoint> neighbours = new HashSet<>();
        data.entrySet().stream().forEach(trajectory -> {
             neighbours.addAll(getNeighbours(trajectory.getValue().stream().filter(notProcessed).collect(Collectors.toList()), point, epsilon));
        });

        if (neighbours.size() >= minPts) {
            // remove core object from its neighbours list
            neighbours.remove(point);
            T coreDistance = neighbours.stream()
                    .map(neighbour -> getDistance(point, neighbour))
                    .min(T::compareTo).orElseThrow(NoSuchElementException::new);
            // if to core object hasn't been assigned reachability distance, assign its core distance
            if (point.getReachabilityDistance() == null) {
                point.setReachabilityDistance(coreDistance);
            }
            neighbours.forEach(neighbour -> neighbour.setReachabilityDistance(getDistance(point, neighbour)));
            seedList.addAll(neighbours);
        }
        point.setProcessed(true);
        point.setOrder(order);
        seedList.remove(point);

        return seedList.stream().distinct().filter(notProcessed).collect(Collectors.toList());
    }

    private List<OpticsPoint> expandCluster(OpticsPoint point, Map<Integer, List<OpticsPoint>> database, T epsilon, Double epsilonSpatial, int minPts, List<OpticsPoint> seedList, int order) {
        Map<Integer, List<OpticsPoint>> data = database.entrySet().stream()
                .filter(entry -> entry.getKey() != point.getModId())
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));
        Set<OpticsPoint> neighbours = new HashSet<>();
        data.entrySet().stream().forEach(trajectory -> {
             neighbours.addAll(getNeighbours(trajectory.getValue().stream().filter(notProcessed).collect(Collectors.toList()), point, epsilon, epsilonSpatial));
        });

        if (neighbours.size() >= minPts) {
            // remove core object from its neighbours list
            neighbours.remove(point);
            SpatialTemporalDim coreDistance = neighbours.stream()
                    .map(neighbour -> getSpatialTemporalDistance(point, neighbour))
                    .min(Comparator.comparing(SpatialTemporalDim::getTemporalDim).thenComparing(SpatialTemporalDim::getSpatialDim))
                    .orElseThrow(NoSuchElementException::new);
            // if to core object hasn't been assigned reachability distance, assign its core distance
            if (point.getReachabilityDistanceTwoDim() == null) {
                point.setReachabilityDistanceTwoDim(coreDistance);
            }
            neighbours.forEach(neighbour -> neighbour.setReachabilityDistanceTwoDim(getSpatialTemporalDistance(point, neighbour)));
            seedList.addAll(neighbours);
        }
        point.setProcessed(true);
        point.setOrder(order);
        seedList.remove(point);

        return seedList.stream().distinct().filter(notProcessed).collect(Collectors.toList());
    }

    private Set<OpticsPoint> getNeighbours(List<OpticsPoint> points, OpticsPoint point, T epsilon) {
        Set<OpticsPoint> neighbours = points.stream().filter(record -> isNeighbour(point, record, epsilon)).collect(Collectors.toSet());
        neighbours.add(point);
        return neighbours;
    }

    private boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, T epsilon) {
        return getDistance(pointP, pointQ).compareTo(epsilon) <= 0;
    }

    protected abstract T getDistance(OpticsPoint pointP, OpticsPoint pointQ);

    protected abstract SpatialTemporalDim getSpatialTemporalDistance(OpticsPoint pointP, OpticsPoint pointQ);

    private Set<OpticsPoint> getNeighbours(List<OpticsPoint> points, OpticsPoint point, T epsilonTemporal, Double epsilonSpatial) {
        Set<OpticsPoint> neighbours = points.stream().filter(record -> isNeighbour(point, record, epsilonTemporal, epsilonSpatial)).collect(Collectors.toSet());
        neighbours.add(point);
        return neighbours;
    }

    protected boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, T epsilonTemporal, Double epsilonSpatial) {
        return isNeighbour(pointP, pointQ, epsilonTemporal);
    }

    protected void asSortedList(List<OpticsPoint> list) {
        list.sort(Comparator.comparing(e -> e.getReachabilityDistance()));
    }
}

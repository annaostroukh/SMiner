package com.sminer.service;

import com.sminer.model.OpticsPoint;
import com.sminer.model.Record;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class OpticsImpl implements IOptics {

    private static final Function<List<Record>, Map<Integer, List<OpticsPoint>>> recordsToOpticsPointsById = (records ->
            records.stream()
                    .map(record -> new OpticsPoint(record.getModId(), record.getTimestamp(), record.getLongitude(), record.getLattitude()))
                    .collect(groupingBy(OpticsPoint::getModId))
    );
    private static final Predicate<OpticsPoint> notProcessed = (OpticsPoint point) -> !point.isProcessed();

    @Override
    public List<OpticsPoint> runTemporalOPTICS(List<Record> records, Integer epsilon, int minPts) {
        int order = 0;
        Set<OpticsPoint> seedListNew = new HashSet<>();
        List<OpticsPoint> seedListOld = new ArrayList<>();
        Map<Integer, List<OpticsPoint>> points = recordsToOpticsPointsById.apply(records);

        for (Map.Entry<Integer, List<OpticsPoint>> entry : points.entrySet()) {
            for (OpticsPoint point : entry.getValue().stream().filter(notProcessed).collect(Collectors.toList())) {
                seedListNew.clear();
                seedListNew.addAll(expandCluster(point, points, epsilon, minPts, new ArrayList<>(), order));
                order++;
                if (!seedListNew.isEmpty()) {
                    for (OpticsPoint seedPoint : asSortedList(seedListNew).stream().filter(notProcessed).collect(Collectors.toList())) {
                        seedListOld.clear();
                        seedListOld.addAll(asSortedList(seedListNew).stream().filter(notProcessed).collect(Collectors.toList()));
                        seedListNew.addAll(expandCluster(seedPoint, points, epsilon, minPts, seedListOld, order));
                        //seedListNew.stream().sorted(Comparator.comparing(e -> e.getReachabilityDistance())).collect(Collectors.toList());
                        order++;
                        if (seedListOld.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }
        return points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Double> runSpatialOPTICS(List<Record> records, Double epsilon, int minPts) {
        return null;
    }

    private List<OpticsPoint> asSortedList(Set<OpticsPoint> set) {
        List<OpticsPoint> list = new ArrayList<>(set);
        return (List<OpticsPoint>) list.stream().sorted(Comparator.comparing(e -> e.getReachabilityDistance())).collect(Collectors.toList());
    }

    private List<OpticsPoint> expandCluster(OpticsPoint point, Map<Integer, List<OpticsPoint>> database, Integer epsilon, int minPts, List<OpticsPoint> seedList, int order) {
        // skip trajectory to which current data point belongs
        Map<Integer, List<OpticsPoint>> data = database.entrySet().stream()
                .filter(entry -> entry.getKey() != point.getModId())
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));
        data.entrySet().stream().forEach(trajectory -> {
            Set<OpticsPoint> neighbours = getNeighbours(trajectory.getValue().stream().filter(notProcessed).collect(Collectors.toList()), point, epsilon);
            point.setProcessed(true);
            point.setOrder(order);
            seedList.remove(point);

            if (neighbours.size() >= minPts) {
                // remove core object from its neighbours list
                neighbours.remove(point);
                Integer coreDistance = neighbours.stream()
                        .map(neighbour -> getTemporalDistance(point, neighbour))
                        .min(Integer::compareTo).orElseThrow(NoSuchElementException::new);
                // if to core object hasn't been assigned reachability distance, assign its core distance
                if (point.getReachabilityDistance() == null) {
                    point.setReachabilityDistance(coreDistance);
                }
                neighbours.forEach(neighbour -> neighbour.setReachabilityDistance(getTemporalDistance(point, neighbour)));
                seedList.addAll(neighbours);
            }
        });
        return seedList;
    }

    private Set<OpticsPoint> getNeighbours(List<OpticsPoint> points, OpticsPoint point, Integer epsilon) {
        Set<OpticsPoint> neighbours = points.stream().filter(record -> isNeighbour(point, record, epsilon)).collect(Collectors.toSet());
        neighbours.add(point);
        return neighbours;
    }

    private Set<OpticsPoint> getNeighbours(List<OpticsPoint> points, OpticsPoint point, Double epsilon) {
        return points.stream().filter(record -> isNeighbour(point, record, epsilon)).collect(Collectors.toSet());
    }

    private boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, Integer epsilon) {
        return getTemporalDistance(pointP, pointQ) <= epsilon;
    }

    private boolean isNeighbour(OpticsPoint pointP, OpticsPoint pointQ, Double epsilon) {
        return getSpatialDistance(pointP, pointQ) <= epsilon;
    }

    private Double getSpatialDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return Math.sqrt(Math.pow((pointP.getLongitude() - pointQ.getLongitude()), 2) + Math.pow((pointP.getLattitude() - pointQ.getLattitude()), 2));
    }

    private Integer getTemporalDistance(OpticsPoint pointP, OpticsPoint pointQ) {
        return (int) Math.sqrt(Math.pow(Duration.between(pointP.getTimestamp().toInstant(), pointQ.getTimestamp().toInstant()).toMinutes(), 2));
    }
}

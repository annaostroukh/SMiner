package com.sminer.service.STDBSCAN;

import com.sminer.model.Record;
import com.sminer.model.STDBSCANpoint;
import org.apache.commons.lang3.time.DateUtils;
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
public class STDBSCANImpl implements ISTDBSCAN {
    private static final double DEGREE_LENGTH = 110.25;
    /**
     * Function converting Records to STDBSCAN data structure and grouping them by moving object Id
     */
    private static final Function<List<Record>, Map<Integer, List<STDBSCANpoint>>> recordsToSTDBSCANPointsById = (records ->
            records.stream()
                    .map(record -> {
                        STDBSCANpoint point = new STDBSCANpoint(record.getModId(), record.getTimestamp(), record.getLongitude(), record.getLattitude());
                        point.setStopDuration(record.getStopDuration());
                        return point;
                    })
                    .collect(groupingBy(STDBSCANpoint::getModId))
    );
    /**
     * Valid point is a point which does not belong to a cluster and isn't marked as noise
     */
    private static final Predicate<STDBSCANpoint> isValid = (STDBSCANpoint point) -> point.getClusterId() == null
            && !point.isNoise();

    @Override
    public List<STDBSCANpoint> runSTDBSCAN(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts) {
        int clusterId = 0;
        List<STDBSCANpoint> neighboursNew = new ArrayList<>();
        Set<STDBSCANpoint> neighboursOld = new HashSet<>();
        Map<Integer, List<STDBSCANpoint>> points = recordsToSTDBSCANPointsById.apply(records);

        for (Map.Entry<Integer, List<STDBSCANpoint>> entry : points.entrySet()) {
            for (STDBSCANpoint point: entry.getValue().stream().filter(isValid).collect(Collectors.toList())) {
                neighboursNew.clear();
                neighboursNew.addAll(expandCluster(point, points, epsilonTemporal, epsilonSpatial, new HashSet<>(), minPts, clusterId));
                if (!neighboursNew.isEmpty()) {
                    for (ListIterator<STDBSCANpoint> it = neighboursNew.stream().filter(p -> !p.isProcessed()).collect(Collectors.toList()).listIterator(); it.hasNext();) {
                        STDBSCANpoint candidatePoint = it.next();
                        neighboursOld.clear();
                        neighboursOld.addAll(neighboursNew.stream().filter(p -> !p.isProcessed()).collect(Collectors.toList()));
                        for (int i = 0; i < expandCluster(candidatePoint, points, epsilonTemporal, epsilonSpatial, neighboursOld, minPts, clusterId).size(); i++) {
                            it.add(expandCluster(candidatePoint, points, epsilonTemporal, epsilonSpatial, neighboursOld, minPts, clusterId)
                                    .stream().collect(Collectors.toList()).get(i));
                        }
                        if (neighboursOld.isEmpty()) {
                            clusterId = verifyCluster(points, epsilonTemporal, clusterId);
                            clusterId++;
                            break;
                        }
                    }
                }
            }
        }
        return points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private Set<STDBSCANpoint> expandCluster(STDBSCANpoint point, Map<Integer, List<STDBSCANpoint>> database, Integer epsilonTemporal,
                                              Double epsilonSpatial, Set<STDBSCANpoint> neighboursSet, int minPts, int clusterId) {
        // skip trajectory to which current data point belongs
        Map<Integer, List<STDBSCANpoint>> data = database.entrySet().stream()
                .filter(entry -> entry.getKey() != point.getModId())
                .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));
        Set<STDBSCANpoint> neighbours = new HashSet<>();
        data.entrySet().stream().forEach(trajectory -> {
            neighbours.addAll(getNeighbours(trajectory.getValue().stream().filter(isValid).collect(Collectors.toList()),
                    point, epsilonTemporal, epsilonSpatial));
        });

        if (neighbours.size() >= minPts) {
            neighbours.stream().forEach(neighbour -> neighbour.setClusterId(clusterId));
            neighbours.remove(point);
            neighboursSet.addAll(neighbours);
        } else {
            if (point.getClusterId() == null) {
                point.setNoise(true);
            }
        }
        neighboursSet.remove(point);
        point.setProcessed(true);
        return neighboursSet;
    }

    private Set<STDBSCANpoint> getNeighbours(List<STDBSCANpoint> points, STDBSCANpoint point, Integer epsilonTemporal, Double epsilonSpatial) {
        Set<STDBSCANpoint> neighbours = points.stream().filter(record -> isNeighbour(point, record, epsilonTemporal, epsilonSpatial)).collect(Collectors.toSet());
        neighbours.add(point);
        return neighbours;
    }

    private boolean isNeighbour(STDBSCANpoint pointP, STDBSCANpoint pointQ, Integer epsilonTemporal, Double epsilonSpatial) {
        return getTemporalDistance(pointP, pointQ) <= epsilonTemporal &&
                getSpatialDistance(pointP, pointQ) <= epsilonSpatial;
    }

    private Double getSpatialDistance(STDBSCANpoint pointP, STDBSCANpoint pointQ) {
        double deltaX = pointP.getLattitude() - pointQ.getLattitude();
        double deltaY = (pointP.getLongitude() - pointQ.getLongitude()) * Math.cos(pointQ.getLattitude());
        return DEGREE_LENGTH * Math.sqrt((Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
    }

    private Integer getTemporalDistance(STDBSCANpoint pointP, STDBSCANpoint pointQ) {
        return (int) Math.sqrt(Math.pow(Duration.between(pointP.getTimestamp().toInstant(), pointQ.getTimestamp().toInstant()).toMinutes(), 2));
    }

    /**
     * Utility function verifying data points in cluster.
     * In case of incorrect grouping due to iterator's work in runSTDBSCAN, splits current cluster into smaller ones with proper grouping
     * @param points input dataset of records
     * @param epsilonTemporal time limit to group a cluster
     * @param clusterId current cluster Id
     * @return current cluster's order
     */
    private int verifyCluster(Map<Integer, List<STDBSCANpoint>> points, Integer epsilonTemporal, final int clusterId) {
        Map<Timestamp, List<STDBSCANpoint>> clusterData = points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList()).stream()
                .filter(stdbscaNpoint -> stdbscaNpoint.getClusterId() != null && stdbscaNpoint.getClusterId() == clusterId)
                .collect(groupingBy(STDBSCANpoint::getTimestamp));
        List<Timestamp> sortedTimestamps = clusterData.keySet().stream().sorted(Comparator.comparing(Timestamp::getTime)).collect(Collectors.toList());
        Iterator<Timestamp> it = sortedTimestamps.iterator();
        Timestamp currentTimestamp = null;
        Timestamp nextTimestamp;
        int nextCluster = clusterId;
        while (it.hasNext()) {
            nextTimestamp = it.next();
            if (currentTimestamp != null) {
                if (Duration.between(currentTimestamp.toInstant(), nextTimestamp.toInstant()).toMinutes() > epsilonTemporal) {
                    nextCluster++;
                    Instant finalNextTimestamp = nextTimestamp.toInstant().truncatedTo(ChronoUnit.DAYS);
                    int finalNextCluster = nextCluster;
                    points.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList()).stream()
                            .filter(stdbscaNpoint -> stdbscaNpoint.getClusterId() != null && stdbscaNpoint.getClusterId() == clusterId)
                            .filter(stdbscaNpoint -> stdbscaNpoint.getTimestamp().toInstant().truncatedTo(ChronoUnit.DAYS).compareTo(finalNextTimestamp) == 0)
                            .forEach(point -> point.setClusterId(finalNextCluster));
                }
            }
            currentTimestamp = nextTimestamp;
        }
        return nextCluster;
    }
}

package com.sminer.service.dataAnalysis;

import com.sminer.model.*;
import com.sminer.service.OPTICS.SpatialOpticsImpl;
import com.sminer.service.OPTICS.SpatialTemporalOpticsImpl;
import com.sminer.service.OPTICS.TemporalOpticsImpl;
import com.sminer.service.STDBSCAN.StdbscanImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class DataAnalysisServiceImpl implements IDataAnalysisService{

    @Autowired
    private TemporalOpticsImpl temporalOpticsImpl;

    @Autowired
    private SpatialOpticsImpl spatialOpticsImpl;

    @Autowired
    private SpatialTemporalOpticsImpl spatialTemporalOpticsImpl;

    @Autowired
    private StdbscanImpl stdbscan;

    @Override
    public List<Record> extractStopsFromRecordsByTreshold(List<Record> records, int minDurationInMin, int maxDurationInMin) {
        AtomicInteger counter = new AtomicInteger(0);
        return  // grouping records by modId to shape trajectories
                records.stream().collect(groupingBy(Record::getModId))
                // getStopsFromTrajectory applied to each trajectory to extract stops
                .entrySet().stream().map(entry -> getStopsFromTrajectory(entry.getValue()))
                .collect(Collectors.toList())
                .stream().flatMap(Collection::stream)
                .collect(Collectors.toList())
                .stream().map(record -> {
                    if (maxDurationInMin != 0) {
                        if ((Math.abs(record.getStopDuration().toMinutes()) >= minDurationInMin &&
                                Math.abs(record.getStopDuration().toMinutes()) <= maxDurationInMin)) {
                            record.setStop(true);
                            record.setStopId(counter.getAndIncrement());
                        }
                    } else if (Math.abs(record.getStopDuration().toMinutes()) >= minDurationInMin) {
                        record.setStop(true);
                        record.setStopId(counter.getAndIncrement());
                    }
                    return record;
                }).filter(Record::isStop).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Integer> getPlotByTemporalDim(List<Record> records, Integer epsilon, int minPts) {
        return temporalOpticsImpl.runOneDimensionOptics(records, epsilon, minPts).stream().map(point -> {
            if (point.getReachabilityDistance() == null) {
                point.setReachabilityDistance(-1);
            }
            return point;
        }).collect(Collectors.toMap(OpticsPoint::getOrder, point -> (Integer) point.getReachabilityDistance()));
    }

    @Override
    public Map<Integer, Double> getPlotBySpatialDim(List<Record> records, Double epsilon, int minPts) {
        return spatialOpticsImpl.runOneDimensionOptics(records, epsilon, minPts).stream().map(point -> {
            if (point.getReachabilityDistance() == null) {
                point.setReachabilityDistance(-1.0);
            }
            return point;
        }).collect(Collectors.toMap(point -> point.getOrder(), point -> (Double) point.getReachabilityDistance()));
    }

    @Override
    public Map<Integer, SpatialTemporalDim> getPlotBySpatialTemporalDim(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts) {
        return spatialTemporalOpticsImpl.runSpatialTemporalOptics(records, epsilonTemporal, epsilonSpatial, minPts).stream().map(point -> {
            if (point.getReachabilityDistanceTwoDim() == null) {
                point.setReachabilityDistanceTwoDim(new SpatialTemporalDim(-1, -1.0));
            }
            return point;
        }).collect(Collectors.toMap(point -> point.getOrder(), point -> point.getReachabilityDistanceTwoDim()));
    }

    @Override
    public Map<Integer, ModCluster> getModClusters(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts) {
        List<ModCluster> clusters = new ArrayList<>();
        Map<Integer, List<StdbscanPoint>> map = stdbscan.runSTDBSCAN(records, epsilonTemporal, epsilonSpatial, minPts).stream()
                // filter noise points from the dataset
                .filter(stdbscaNpoint -> !stdbscaNpoint.isNoise())
                .collect(groupingBy(StdbscanPoint::getClusterId));
        map.forEach((k, v) -> clusters.add(new ModCluster(k.intValue(),
                        v.stream().min(Comparator.comparing(StdbscanPoint::getLattitude)).orElseThrow(NoSuchElementException::new).getLattitude(),
                        v.stream().max(Comparator.comparing(StdbscanPoint::getLattitude)).orElseThrow(NoSuchElementException::new).getLattitude(),
                        v.stream().min(Comparator.comparing(StdbscanPoint::getLongitude)).orElseThrow(NoSuchElementException::new).getLongitude(),
                        v.stream().max(Comparator.comparing(StdbscanPoint::getLongitude)).orElseThrow(NoSuchElementException::new).getLongitude(),
                        v.stream().map(point -> {
                            Record record = new Record(point.getModId(), point.getTimestamp(), point.getLongitude(), point.getLattitude());
                            record.setStopDuration(point.getStopDuration());
                            record.setStopId(point.getStopId());
                            return record;
                        }).collect(Collectors.toList()))));
        clusters.forEach(cluster -> cluster.setClusterCentroidLat(getClusterCentroid(cluster.getPoints())[0]));
        clusters.forEach(cluster -> cluster.setClusterCentroidLon(getClusterCentroid(cluster.getPoints())[1]));
        return clusters.stream().collect(Collectors.toMap(ModCluster::getClusterId, Function.identity()));
    }

    private List<Record> getStopsFromTrajectory(List<Record> records) {
        Iterator<Record> it = records.iterator();
        Record currentRecord = null;
        Record nextRecord = null;
        while (it.hasNext()) {
            nextRecord = it.next();
            if (currentRecord != null) {
                currentRecord.setStopDuration(getStopDuration(currentRecord.getTimestamp(), nextRecord.getTimestamp()));
            }
            currentRecord = nextRecord;
        }
        nextRecord.setStopDuration(Duration.ZERO);
        return records;
    }

    private Duration getStopDuration(Timestamp current, Timestamp next) {
        return Duration.between(current.toInstant(), next.toInstant());
    }

    private double[] getClusterCentroid(List<Record> points) {
        List<double[]> cartesianAllPoints = new ArrayList<>();
        double[] cartesian;
        int totalWeight = 0;
        double avgX;
        double avgY;
        double avgZ;
        for (Record point : points) {
           cartesian = getCartesian(point);
           totalWeight = totalWeight + 2;
           cartesianAllPoints.add(cartesian);
        }
        avgX = cartesianAllPoints.stream().mapToDouble(e -> e[0]).sum() / totalWeight;
        avgY = cartesianAllPoints.stream().mapToDouble(e -> e[1]).sum() / totalWeight;
        avgZ = cartesianAllPoints.stream().mapToDouble(e -> e[2]).sum() / totalWeight;

        double lon = Math.atan2(avgY, avgX);
        double lat = Math.atan2(avgZ, Math.sqrt(Math.pow(avgX, 2) + Math.pow(avgY, 2)));

        return new double[]{(lat * 180) / 3.14, (lon * 180) / 3.14};
    }

    private double[] getCartesian(Record point) {
        double lat1 = (point.getLattitude() * 3.14) / 180;
        double lon1 = (point.getLongitude() * 3.14) / 180;
        double x = Math.cos(lat1) * Math.cos(lon1);
        double y = Math.cos(lat1) * Math.sin(lon1);
        double z = Math.sin(lat1);
        double[] cartesian = {x, y, z};
        return cartesian;
    }
}

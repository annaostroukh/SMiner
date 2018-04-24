package com.sminer.service;

import com.sminer.model.*;
import com.sminer.service.OPTICS.SpatialOpticsImpl;
import com.sminer.service.OPTICS.SpatialTemporalOpticsImpl;
import com.sminer.service.OPTICS.TemporalOpticsImpl;
import com.sminer.service.STDBSCAN.STDBSCANImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
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
    private STDBSCANImpl stdbscan;

    @Override
    public List<Record> extractStopsFromRecordsByTreshold(final List<Record> records, int minDurationInMin, int maxDurationInMin) {
        return records.stream().collect(groupingBy(Record::getModId))
                .entrySet().stream().map(entry -> getStopsFromTrajectory(entry.getValue()))
                .collect(Collectors.toList())
                .stream().flatMap(Collection::stream)
                .collect(Collectors.toList())
                .stream().map(record -> {
                    if (maxDurationInMin != 0) {
                        record.setStop(Math.abs(record.getStopDuration().toMinutes()) >= minDurationInMin &&
                                Math.abs(record.getStopDuration().toMinutes()) <= maxDurationInMin);
                    } else {
                        record.setStop(Math.abs(record.getStopDuration().toMinutes()) >= minDurationInMin);
                    }
                    return record;
                }).filter(Record::isStop).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Integer> getPlotByTemporalDim(List<Record> records, Integer epsilon, int minPts) {
        return temporalOpticsImpl.runOneDimensionOPTICS(records, epsilon, minPts).stream().map(point -> {
            if (point.getReachabilityDistance() == null) {
                point.setReachabilityDistance(0);
            }
            return point;
        }).collect(Collectors.toMap(OpticsPoint::getOrder, point -> (Integer) point.getReachabilityDistance()));
    }

    @Override
    public Map<Integer, Double> getPlotBySpatialDim(List<Record> records, Double epsilon, int minPts) {
        return spatialOpticsImpl.runOneDimensionOPTICS(records, epsilon, minPts).stream().map(point -> {
            if (point.getReachabilityDistance() == null) {
                point.setReachabilityDistance(0.0);
            }
            return point;
        }).collect(Collectors.toMap(point -> point.getOrder(), point -> (Double) point.getReachabilityDistance()));
    }

    @Override
    public Map<Integer, SpatialTemporalDim> getPlotBySpatialTemporalDim(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts) {
        return spatialTemporalOpticsImpl.runSpatialTemporalOPTICS(records, epsilonTemporal, epsilonSpatial, minPts).stream().map(point -> {
            if (point.getReachabilityDistanceTwoDim() == null) {
                point.setReachabilityDistanceTwoDim(new SpatialTemporalDim(0, 0.0));
            }
            return point;
        }).collect(Collectors.toMap(point -> point.getOrder(), point -> point.getReachabilityDistanceTwoDim()));
    }

    @Override
    public Map<Integer, ModCluster> getModClusters(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts) {
        List<ModCluster> clusters = new ArrayList<>();
        Map<Integer, List<STDBSCANpoint>> map = stdbscan.runSTDBSCAN(records, epsilonTemporal, epsilonSpatial, minPts).stream()
                // filter noise points from the dataset
                .filter(stdbscaNpoint -> !stdbscaNpoint.isNoise())
                .collect(groupingBy(STDBSCANpoint::getClusterId));
        map.forEach((k, v) -> clusters.add(new ModCluster(k.intValue(),
                        v.stream().min(Comparator.comparing(STDBSCANpoint::getLattitude)).orElseThrow(NoSuchElementException::new).getLattitude(),
                        v.stream().max(Comparator.comparing(STDBSCANpoint::getLattitude)).orElseThrow(NoSuchElementException::new).getLattitude(),
                        v.stream().min(Comparator.comparing(STDBSCANpoint::getLongitude)).orElseThrow(NoSuchElementException::new).getLongitude(),
                        v.stream().max(Comparator.comparing(STDBSCANpoint::getLongitude)).orElseThrow(NoSuchElementException::new).getLongitude(),
                        v.stream().map(point -> {
                            Record record = new Record(point.getModId(), point.getTimestamp(), point.getLongitude(), point.getLattitude());
                            record.setStopDuration(point.getStopDuration());
                            return record;
                        }).collect(Collectors.toList()))));
        //clusters.forEach(cluster -> cluster.setExtractLocationsURL(getSemanticLocationsQuery(cluster.getLattitudeMax(), cluster.getLongitudeMax(),
                //cluster.getLattitudeMin(), cluster.getLongitudeMin())));
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
}

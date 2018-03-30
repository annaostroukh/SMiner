package com.sminer.service;

import com.sminer.model.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class DataAnalysisServiceImpl implements IDataAnalysisService{

    @Autowired
    OpticsImpl opticsImpl;

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
        return opticsImpl.runTemporalOPTICS(records, epsilon, minPts).stream().map(point -> {
            if (point.getReachabilityDistance() == null) {
                // noise points
                point.setReachabilityDistance(0);
            }
            return point;
        }).collect(Collectors.toMap(point -> point.getOrder(), point -> (Integer)point.getReachabilityDistance()));
    }

    @Override
    public Map<Integer, Double> getPlotBySpatialDim(List<Record> records) {
        return null;
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

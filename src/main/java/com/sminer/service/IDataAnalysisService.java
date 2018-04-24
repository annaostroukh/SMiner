package com.sminer.service;

import com.sminer.model.ModCluster;
import com.sminer.model.Record;
import com.sminer.model.SpatialTemporalDim;

import java.util.List;
import java.util.Map;

public interface IDataAnalysisService {

    /**
     * Returns GPS records with computed stops duration and marked with "stop" flag according to set thresholds
     * @param records
     * @param minDurationInMin minimum threshold for vehicle stop
     * @param maxStopDuration (optional) maximum threshold for vehicle stop
     * @return GPS records
     */
    List<Record> extractStopsFromRecordsByTreshold(final List<Record> records, int minDurationInMin, int maxStopDuration);

    Map<Integer, Integer> getPlotByTemporalDim(final List<Record> records, final Integer epsilon, final int minPts);

    Map<Integer, Double> getPlotBySpatialDim(final List<Record> records, final Double epsilon, final int minPts);

    Map<Integer, SpatialTemporalDim> getPlotBySpatialTemporalDim(final List<Record> records, final Integer epsilonTemporal, final Double epsilonSpatial, final int minPts);

    Map<Integer, ModCluster> getModClusters(final List<Record> records, final Integer epsilonTemporal, final Double epsilonSpatial, final int minPts);
}

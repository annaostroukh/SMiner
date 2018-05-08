package com.sminer.service.dataAnalysis;

import com.sminer.model.ModCluster;
import com.sminer.model.Record;
import com.sminer.model.SpatialTemporalDim;

import java.util.List;
import java.util.Map;

public interface IDataAnalysisService {

    /**
     * Returns GPS records with computed stops duration and marked with "stop" flag according to set thresholds
     * @param records dataset converted to Record object
     * @param minDurationInMin minimum threshold for vehicle stop
     * @param maxStopDuration (optional) maximum threshold for vehicle stop
     * @return GPS records
     */
    List<Record> extractStopsFromRecordsByTreshold(List<Record> records, int minDurationInMin, int maxStopDuration);

    /**
     * Returns data structure appropriate for building one dimensional graph where key is a processing order, value is reachability distance in dataset
     * @param records dataset converted to Record object
     * @param epsilon neighbourhood value
     * @param minPts minimum amount of points in cluster
     * @return data structure for one dimensional graph
     */
    Map<Integer, Integer> getPlotByTemporalDim(List<Record> records, Integer epsilon, int minPts);
    Map<Integer, Double> getPlotBySpatialDim(List<Record> records, Double epsilon, int minPts);

    /**
     * Returns data structure appropriate for building two dimensional graph where key is a processing order, value is reachability distance in dataset by two dimensions
     * @param records dataset converted to Record object
     * @param epsilonTemporal neighbourhood value for temporal dimension
     * @param epsilonSpatial neighbourhood value for spatial dimension
     * @param minPts minimum amount of points in cluster
     * @return data structure for two dimensional graph
     */
    Map<Integer, SpatialTemporalDim> getPlotBySpatialTemporalDim(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts);

    /**
     * Getting clustered data, where key is a cluster Id, value is a cluster object
     * @param records dataset converted to Record object
     * @param epsilonTemporal neighbourhood value for temporal dimension
     * @param epsilonSpatial neighbourhood value for spatial dimension
     * @param minPts minimum amount of points in cluster
     * @return clustered data
     */
    Map<Integer, ModCluster> getModClusters(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts);
}

package com.sminer.service;

import com.sminer.model.OpticsPoint;
import com.sminer.model.Record;

import java.util.List;
import java.util.Map;

/**
 * Interface for OPTICS algorithm implementation
 */
public interface IOptics {

    /**
     * Implementation of OPTICS algorithm running on temporal data
     * @param trajectories GPS records grouped to trajectories by modId parameter
     * @param epsilon neighbourhood value
     * @param minPts minimal amount of points to shape a cluster
     */
    List<OpticsPoint> runTemporalOPTICS(List<Record> trajectories, Integer epsilon, int minPts);

    /**
     * Implementation of OPTICS algorithm running on spatial data
     * @param records database of GPS records
     * @param epsilon neighbourhood value
     * @param minPts minimal amount of points to shape a cluster
     */
    Map<Integer, Double> runSpatialOPTICS(List<Record> records, Double epsilon, int minPts);
}

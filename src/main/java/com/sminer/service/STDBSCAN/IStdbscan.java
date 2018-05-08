package com.sminer.service.STDBSCAN;

import com.sminer.model.Record;
import com.sminer.model.StdbscanPoint;

import java.util.List;

public interface IStdbscan {
    /**
     * Runs ST-DBSCAN clustering  method on a dataset
     * @param records dataset converted to Records object
     * @param epsilonTemporal neighbourhood value for temporal dimension
     * @param epsilonSpatial neighbourhood value for spatial dimension
     * @param minPts minimum number of points in a cluster
     * @return list of points with set cluster Id
     */
    List<StdbscanPoint> runSTDBSCAN(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int minPts);
}

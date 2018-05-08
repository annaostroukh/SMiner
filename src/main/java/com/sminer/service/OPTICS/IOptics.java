package com.sminer.service.OPTICS;

import com.sminer.model.OpticsPoint;
import com.sminer.model.Record;

import java.util.List;
import java.util.Map;

public interface IOptics<T> {

    /**
     * Implementation of OPTICS algorithm running on one dimension (Spatial or Temporal)
     * @param records GPS records
     * @param epsilon neighbourhood value. Generic type <T> represents spatial or temporal parameter of epsilon
     * @param minPts minimal amount of points to shape a cluster
     */
    List<OpticsPoint> runOneDimensionOptics(List<Record> records, T epsilon, int minPts);

    /**
     * Implementation of OPTICS algorithm running on two dimensions (Spatial and Temporal)
     * @param records GPS records
     * @param epsilonTemporal neighbourhood value for temporal dimension
     * @param epsilonSpatial neighbourhood value for spatial dimension
     * @param minPts minimal amount of points to shape a cluster
     */
    List<OpticsPoint> runSpatialTemporalOptics(List<Record> records, T epsilonTemporal, Double epsilonSpatial, int minPts);
}

package com.sminer.service.STDBSCAN;

import com.sminer.model.Record;
import com.sminer.model.STDBSCANpoint;

import java.util.List;

public interface ISTDBSCAN {
    List<STDBSCANpoint> runSTDBSCAN(List<Record> records, Integer epsilonTemporal, Double epsilonSpatial, int epsilon);
}

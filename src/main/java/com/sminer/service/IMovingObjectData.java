package com.sminer.service;

import com.sminer.model.Record;

import java.util.HashMap;

/**
 * Interface for handling basic operations with moving object data
 */
public interface IMovingObjectData {
    HashMap<Integer, Record> readTrajectoryData();
}

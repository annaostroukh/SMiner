package com.sminer.service;

import com.sminer.model.Trajectory;

import java.util.HashMap;

/**
 * Interface for handling basic operations with moving object data
 */
public interface IMovingObjectData {
    HashMap<Integer, Trajectory> readTrajectoryData();
}

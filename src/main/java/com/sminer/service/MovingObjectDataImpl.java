package com.sminer.service;

import com.sminer.model.Trajectory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Service for handling basic moving object data operations
 */
@Service
public class MovingObjectDataImpl implements IMovingObjectData{
    @Override
    public HashMap<Integer, Trajectory> readTrajectoryData() {
        return null;
    }
}

package com.sminer.service;

import com.sminer.model.Record;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Service for handling basic moving object data operations
 */
@Service
public class MovingObjectDataImpl implements IMovingObjectData{
    @Override
    public HashMap<Integer, Record> readTrajectoryData() {
        return null;
    }
}

package com.sminer.controller;

import com.sminer.model.*;
import com.sminer.service.DataAnalysisServiceImpl;
import com.sminer.service.DocumentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.groupingBy;


@RestController
@RequestMapping("/rest")
public class RestApiController {

    @Autowired
    private DocumentServiceImpl documentService;

    @Autowired
    private DataAnalysisServiceImpl dataAnalysisService;

    private List<Integer> datasetConfiguration;
    private List<Record> records;
    private List<Record> recordsByStopThreshold;

    @RequestMapping(value = "/datasetConfiguration", method = RequestMethod.POST)
    public ResponseEntity datasetConfiguration(@RequestParam("configuration") List<Integer> configuration) {
        datasetConfiguration = configuration;
       return buildOkResponse(Collections.singletonMap("value", "Configuration saved!"));
    }

    @RequestMapping(value = "/modFileUpload", method = RequestMethod.POST)
    public ResponseEntity<FileStats> uploadModData(
            @RequestParam("file") MultipartFile file) {
        try {
            File uploadedFile = documentService.save(file);
            long totalAmountOfRecords = documentService.countLinesInDocument(uploadedFile);
            long startTime = System.nanoTime();
            records = documentService.parseCsvFileToRecords(uploadedFile, datasetConfiguration);
            long endTime = System.nanoTime();
            long elapsedTimeInMillis = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
            long validAmountOfRecords = records.stream().count();
            return buildOkResponse(new FileStats()
                    .setTotalAmountOfRecords(totalAmountOfRecords)
                    .setValidRecords(validAmountOfRecords)
            .setElapsedTime((double)elapsedTimeInMillis / 1000));
        } catch (Exception e) {
            return buildFailedResponse("Failed to upload " + file.getOriginalFilename());
        }
    }

    @RequestMapping(value = "/extractstops", method = RequestMethod.GET)
    public ResponseEntity extractStopsByThreshold(
            @RequestParam("minStopDuration") int minStopDuration,
            @RequestParam(value = "maxStopDuration", required = false, defaultValue = "0") int maxStopDuration) {
        recordsByStopThreshold = dataAnalysisService.extractStopsFromRecordsByTreshold(records, minStopDuration, maxStopDuration);
        int trajectories = recordsByStopThreshold.stream().collect(groupingBy(Record::getModId)).keySet().size();
        String response = "Found " + recordsByStopThreshold.size() + " stops in " + trajectories + " trajectories";
        return buildOkResponse(Collections.singletonMap("value", response));
    }

    @RequestMapping(value = "/temporalReachabilityPlot", method = RequestMethod.GET)
    public ResponseEntity getPlotByTemporalDimension(
            @RequestParam("epsilonTemporal") int epsilonTemporal,
            @RequestParam("minPtsTemporal") int minPtsTemporal) {
        Map<Integer, Integer> temporalPlot = dataAnalysisService.getPlotByTemporalDim(recordsByStopThreshold, epsilonTemporal, minPtsTemporal);
        return buildOkResponse(Collections.singletonMap("data", temporalPlot));
    }

    @RequestMapping(value = "/spatialReachabilityPlot", method = RequestMethod.GET)
    public ResponseEntity getPlotBySpatialDimension(
            @RequestParam("epsilonSpatial") double epsilonSpatial,
            @RequestParam("minPtsSpatial") int minPtsSpatial) {
        Map<Integer, Double> spatialPlot = dataAnalysisService.getPlotBySpatialDim(recordsByStopThreshold, epsilonSpatial, minPtsSpatial);
        return buildOkResponse(Collections.singletonMap("data", spatialPlot));
    }

    @RequestMapping(value = "/spatialTemporalReachabilityPlot", method = RequestMethod.GET)
    public ResponseEntity getPlotBySpatialDimension(
            @RequestParam("epsilonTemporal") int epsilonTemporal,
            @RequestParam("epsilonSpatial") double epsilonSpatial,
            @RequestParam("minPtsTemporal") int minPtsTemporal) {
        Map<Integer, SpatialTemporalDim> spatialPlot = dataAnalysisService.getPlotBySpatialTemporalDim(recordsByStopThreshold, epsilonTemporal, epsilonSpatial, minPtsTemporal);
        return buildOkResponse(Collections.singletonMap("data", spatialPlot));
    }

    @RequestMapping(value = "/stdbscan", method = RequestMethod.GET)
    public ResponseEntity getSTDBSCANData(
            @RequestParam("epsilonTempSTDBSCAN") int epsilonTempSTDBSCAN,
            @RequestParam("epsilonSpatialSTDBSCAN") double epsilonSpatialSTDBSCAN,
            @RequestParam("minPtsSTDBSCAN") int minPtsSTDBSCAN) {
        Map<Integer, ModCluster> clusters = dataAnalysisService.getModClusters(recordsByStopThreshold, epsilonTempSTDBSCAN, epsilonSpatialSTDBSCAN, minPtsSTDBSCAN);
        return buildOkResponse(Collections.singletonMap("data", clusters));
    }


    private <T> ResponseEntity<T> buildOkResponse(final T response) {
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity buildOkResponse() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    private ResponseEntity buildFailedResponse(String message) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
    }

}

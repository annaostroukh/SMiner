package com.sminer.controller;

import com.sminer.model.*;
import com.sminer.service.dataAnalysis.DataAnalysisServiceImpl;
import com.sminer.service.document.DocumentServiceImpl;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


@RestController
@RequestMapping("/rest")
public class RestApiController {

    @Autowired
    private DocumentServiceImpl documentService;

    @Autowired
    private DataAnalysisServiceImpl dataAnalysisService;

    private List<Integer> datasetConfiguration;
    private List<Record> records = new ArrayList<>();
    private List<Record> recordsByStopThreshold = new ArrayList<>();
    private List<Record> recordsByTimeThreshold = new ArrayList<>();

    @RequestMapping(value = "/datasetConfiguration", method = RequestMethod.POST)
    public ResponseEntity datasetConfiguration(@RequestParam("configuration") List<Integer> configuration) {
        datasetConfiguration = configuration;
       return buildOkResponse(Collections.singletonMap("value", "Configuration saved!"));
    }

    @RequestMapping(value = "/modFileUpload", method = RequestMethod.POST)
    public ResponseEntity<FileStats> uploadModData(
            @RequestParam("file") MultipartFile file) {
        if (!records.isEmpty()) {
            records.clear();
        }
        try {
            File uploadedFile = documentService.saveFile(file);
            long startTime = System.nanoTime();
            Map<Long, List<Record>> mapRecords = documentService.parseCsvFileToRecords(uploadedFile, datasetConfiguration);
            records = mapRecords.values().stream().collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
            long totalAmountOfRecords = mapRecords.keySet().stream().findFirst().get();
            long endTime = System.nanoTime();
            long elapsedTimeInMillis = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
            long validAmountOfRecords = records.stream().count();
            return buildOkResponse(new FileStats()
                    .setTotalAmountOfRecords(totalAmountOfRecords)
                    .setValidRecords(validAmountOfRecords)
            .setElapsedTime((double)elapsedTimeInMillis / 1000));
        } catch (Exception e) {
            return buildFailedResponse("Failed to upload " + file.getOriginalFilename() + "\n Caused by: " + e);
        }
    }

    @RequestMapping(value = "/extractstops", method = RequestMethod.GET)
    public ResponseEntity extractStopsByThreshold(
            @RequestParam("minStopDuration") int minStopDuration,
            @RequestParam(value = "maxStopDuration", required = false, defaultValue = "0") int maxStopDuration) {
        if (!recordsByStopThreshold.isEmpty()) {
            recordsByStopThreshold.clear();
            records.stream().forEach(point -> point.setStop(false));
        }
        recordsByStopThreshold.addAll(dataAnalysisService.extractStopsFromRecordsByTreshold(records, minStopDuration, maxStopDuration));
        int trajectories = recordsByStopThreshold.stream().collect(groupingBy(Record::getModId)).keySet().size();
        String response = "Found " + recordsByStopThreshold.size() + " stops in " + trajectories + " trajectories";
        Map<String, List<Record>> responseMap = new HashMap<>();
        responseMap.put(response, recordsByStopThreshold);
        return buildOkResponse(Collections.singletonMap("value", responseMap));
    }

    @RequestMapping(value = "/extractstopsByTime", method = RequestMethod.GET)
    public ResponseEntity extractStopsByTimeThreshold(
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
            @RequestParam("dateTill") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTill) {
        recordsByTimeThreshold = recordsByStopThreshold.stream().filter(item -> item.getTimestamp().toInstant().truncatedTo(ChronoUnit.DAYS).isAfter(DateUtils.addDays(dateFrom, -1).toInstant()) &&
                item.getTimestamp().toInstant().truncatedTo(ChronoUnit.DAYS).isBefore(DateUtils.addDays(dateTill, 1).toInstant()))
                .collect(Collectors.toList());
        int trajectories = recordsByTimeThreshold.stream().collect(groupingBy(Record::getModId)).keySet().size();
        String response = "Found " + recordsByTimeThreshold.size() + " stops in " + trajectories + " trajectories";
        return buildOkResponse(Collections.singletonMap("value", response));
    }

    @RequestMapping(value = "/temporalReachabilityPlot", method = RequestMethod.GET)
    public ResponseEntity getPlotByTemporalDimension(
            @RequestParam("epsilonTemporal") int epsilonTemporal,
            @RequestParam("minPtsTemporal") int minPtsTemporal) {
        Map<Integer, Integer> temporalPlot;
        if (!recordsByTimeThreshold.isEmpty()){
            temporalPlot = dataAnalysisService.getPlotByTemporalDim(recordsByTimeThreshold, epsilonTemporal, minPtsTemporal);
        } else {
            temporalPlot = dataAnalysisService.getPlotByTemporalDim(recordsByStopThreshold, epsilonTemporal, minPtsTemporal);
        }
        return buildOkResponse(Collections.singletonMap("data", temporalPlot));
    }

    @RequestMapping(value = "/spatialReachabilityPlot", method = RequestMethod.GET)
    public ResponseEntity getPlotBySpatialDimension(
            @RequestParam("epsilonSpatial") double epsilonSpatial,
            @RequestParam("minPtsSpatial") int minPtsSpatial) {
        Map<Integer, Double> spatialPlot;
        if (!recordsByTimeThreshold.isEmpty()){
            spatialPlot = dataAnalysisService.getPlotBySpatialDim(recordsByTimeThreshold, epsilonSpatial, minPtsSpatial);
        } else {
            spatialPlot = dataAnalysisService.getPlotBySpatialDim(recordsByStopThreshold, epsilonSpatial, minPtsSpatial);
        }
        return buildOkResponse(Collections.singletonMap("data", spatialPlot));
    }

    @RequestMapping(value = "/spatialTemporalReachabilityPlot", method = RequestMethod.GET)
    public ResponseEntity getPlotBySpatialDimension(
            @RequestParam("epsilonTemporal") int epsilonTemporal,
            @RequestParam("epsilonSpatial") double epsilonSpatial,
            @RequestParam("minPtsTemporal") int minPtsTemporal) {
        Map<Integer, SpatialTemporalDim> spatialPlot;
        if (!recordsByTimeThreshold.isEmpty()){
            spatialPlot = dataAnalysisService.getPlotBySpatialTemporalDim(recordsByTimeThreshold, epsilonTemporal, epsilonSpatial, minPtsTemporal);
        } else {
            spatialPlot = dataAnalysisService.getPlotBySpatialTemporalDim(recordsByStopThreshold, epsilonTemporal, epsilonSpatial, minPtsTemporal);
        }
        return buildOkResponse(Collections.singletonMap("data", spatialPlot));
    }

    @RequestMapping(value = "/stdbscan", method = RequestMethod.GET)
    public ResponseEntity getSTDBSCANData(
            @RequestParam("epsilonTempSTDBSCAN") int epsilonTempSTDBSCAN,
            @RequestParam("epsilonSpatialSTDBSCAN") double epsilonSpatialSTDBSCAN,
            @RequestParam("minPtsSTDBSCAN") int minPtsSTDBSCAN) {
        Map<Integer, ModCluster> clusters;
        if (!recordsByTimeThreshold.isEmpty()){
            clusters = dataAnalysisService.getModClusters(recordsByTimeThreshold, epsilonTempSTDBSCAN, epsilonSpatialSTDBSCAN, minPtsSTDBSCAN);
        } else {
            clusters = dataAnalysisService.getModClusters(recordsByStopThreshold, epsilonTempSTDBSCAN, epsilonSpatialSTDBSCAN, minPtsSTDBSCAN);
        }
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

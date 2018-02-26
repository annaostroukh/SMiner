package com.sminer.service;

import com.sminer.model.FileStats;
import com.sminer.model.Trajectory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DocumentServiceImpl implements IDocumentService{

    @Override
    public File save(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    @Override
    public List<Trajectory> parseCsvFileToTrajectories(File file) {
        List<Trajectory> trajectories = new ArrayList<>();
        try {
            trajectories = Files.lines(file.toPath(), StandardCharsets.UTF_8).skip(1)
                    .map(s -> {
                        String[] parsedLine = s.split(",");
                        return isValidTrajectory(new Trajectory(Integer.parseInt(parsedLine[0]),
                                isPresent(parsedLine[1]) ? Integer.parseInt(parsedLine[1]) : 0,
                                parseTimestamp(parsedLine[2]),
                                parseTimestamp(parsedLine[3]),
                                isPresent(parsedLine[4]) ? Double.parseDouble(parsedLine[4]) : 0,
                                isPresent(parsedLine[5]) ? Double.parseDouble(parsedLine[5]) : 0,
                                isPresent(parsedLine[6]) ? Double.parseDouble(parsedLine[6]) : 0,
                                isPresent(parsedLine[7]) ? Double.parseDouble(parsedLine[7]) : 0));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trajectories;
    }

    @Override
    public long countLinesInDocument(File file) {
        try {
            return Files.lines(file.toPath(), StandardCharsets.UTF_8).skip(1).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private Trajectory isValidTrajectory(Trajectory t) {
        if (t.getModId() != 0 && t.getTripId() != 0
                && t.getStartTime() != null && t.getEndTime() != null
                && t.getxStart() != 0 && t.getxEnd() != 0
                && t.getyStart() != 0 && t.getyEnd() != 0) {
            return t;
        }
        return null;
    }

    private Timestamp parseTimestamp(String timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(timestamp);
            return new Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean isPresent(String value) {
        return !value.isEmpty();
    }
}

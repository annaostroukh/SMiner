package com.sminer.service;

import com.sminer.model.Record;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<Record> parseCsvFileToRecords(File file, List<Integer> datasetConfiguration) {
        List<Record> records = new ArrayList<>();
        try {
            records = Files.lines(file.toPath(), StandardCharsets.UTF_8).skip(1)
                    .map(s -> {
                        String[] parsedLine = s.split(",");
                        return isValidRecord(new Record(Integer.parseInt(parsedLine[datasetConfiguration.get(0)]),
                                parseTimestamp(parsedLine[datasetConfiguration.get(1)]),
                                isPresent(parsedLine[datasetConfiguration.get(2)]) ? Double.parseDouble(parsedLine[datasetConfiguration.get(2)]) : 0,
                                isPresent(parsedLine[datasetConfiguration.get(3)]) ? Double.parseDouble(parsedLine[datasetConfiguration.get(3)]) : 0));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public long countLinesInDocument(File file) {
        try {
            return Files.lines(file.toPath(), StandardCharsets.UTF_8).skip(1).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private Record isValidRecord(Record t) {
        if (t.getModId() != 0 && t.getTimestamp() != null
                && t.getLongitude() != 0 && t.getLattitude() != 0) {
            return t;
        }
        return null;
    }

    private Timestamp parseTimestamp(String timestamp) {
        // Time pattern considers hours and minutes
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
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

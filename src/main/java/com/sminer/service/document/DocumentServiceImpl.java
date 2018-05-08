package com.sminer.service.document;

import com.sminer.model.Record;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements IDocumentService{

    @Override
    public File saveFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    @Override
    public Map<Long, List<Record>> parseCsvFileToRecords(File file, List<Integer> datasetConfiguration) {
        AtomicInteger atomicInt = new AtomicInteger(0);
        List<Record> records = new ArrayList<>();
        try {
            records = Files.lines(file.toPath(), StandardCharsets.UTF_8).skip(1)
                    .map(s -> {
                        atomicInt.getAndIncrement();
                        if (!s.isEmpty()) {
                            String[] parsedLine = s.split(",");
                            return isValidRecord(new Record(Integer.parseInt(parsedLine[datasetConfiguration.get(0)].replaceAll("\\s+","")),
                                    parseTimestamp(parsedLine[datasetConfiguration.get(1)]),
                                    isPresent(parsedLine[datasetConfiguration.get(2)]) ? Double.parseDouble(parsedLine[datasetConfiguration.get(2)]) : 0,
                                    isPresent(parsedLine[datasetConfiguration.get(3)]) ? Double.parseDouble(parsedLine[datasetConfiguration.get(3)]) : 0));
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<Long, List<Record>> map = new HashMap<>();
        map.put(atomicInt.longValue(), records);

        return map;
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

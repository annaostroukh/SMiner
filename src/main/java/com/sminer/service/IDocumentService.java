package com.sminer.service;

import com.sminer.model.Record;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IDocumentService {
    File save(MultipartFile file) throws IOException;

    List<Record> parseCsvFileToRecords(File file, List<Integer> datasetConfiguration);

    long countLinesInDocument(File file);
}

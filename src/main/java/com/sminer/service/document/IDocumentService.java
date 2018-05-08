package com.sminer.service.document;

import com.sminer.model.Record;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IDocumentService {
    /**
     * Saves uploaded file
     * @param file uploading multipart file
     * @return File entity
     * @throws IOException
     */
    File saveFile(MultipartFile file) throws IOException;

    /**
     * Parses entries from .csv file to Record entity and returns a map, where key is a total number of records in a file
     * @param file File entity
     * @param datasetConfiguration setting for columns to parse
     * @return GPS records converted to Record data structure with total number of rows in a file
     */
    Map<Long, List<Record>> parseCsvFileToRecords(File file, List<Integer> datasetConfiguration);
}

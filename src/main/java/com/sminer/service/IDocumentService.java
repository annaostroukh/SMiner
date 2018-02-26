package com.sminer.service;

import com.sminer.model.Trajectory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IDocumentService {
    File save(MultipartFile file) throws IOException;

    List<Trajectory> parseCsvFileToTrajectories(File file);

    long countLinesInDocument(File file);
}

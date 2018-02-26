package com.sminer.controller;

import com.sminer.model.FileStats;
import com.sminer.model.Trajectory;
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
import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/rest")
public class RestApiController {

    @Autowired
    DocumentServiceImpl documentService;

    @RequestMapping(value = "/modFileUpload", method = RequestMethod.POST)
    public ResponseEntity uploadModData(
            @RequestParam("file")MultipartFile file) throws IOException {
        try {
            File uploadedFile = documentService.save(file);
            long totalAmountOfRows = documentService.countLinesInDocument(uploadedFile);
            long startTime = System.nanoTime();
            List<Trajectory> trajectories = documentService.parseCsvFileToTrajectories(uploadedFile);
            long endTime = System.nanoTime();
            long elapsedTimeInMillis = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
            long validAmountOfRows = trajectories.stream().count();
            return buildOkResponse(new FileStats()
                    .setTotalAmountOfTrajectories(totalAmountOfRows)
                    .setValidTrajectories(validAmountOfRows)
            .setElapsedTime((double)elapsedTimeInMillis / 1000));
        } catch (Exception e) {
            return buildFailedResponse("Fail to upload " + file.getOriginalFilename());
        }
    }

    private ResponseEntity buildOkResponse(Object response) {
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private ResponseEntity buildOkResponse() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    private ResponseEntity buildFailedResponse(String message) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
    }

}

package com.zplus.jobportal.controller;


import com.zplus.jobportal.model.SavedJob;
import com.zplus.jobportal.services.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved-jobs")
public class SavedJobController {

    private final SavedJobService savedJobService;

    @Autowired
    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
    }

    @PostMapping("/{employeeId}/{jobId}")
    public ResponseEntity<String> saveJob(
            @PathVariable Long employeeId,
            @PathVariable Long jobId
    ) {
        savedJobService.saveJob(employeeId, jobId);
        return ResponseEntity.ok("Job saved successfully");
    }

    @DeleteMapping("/{employeeId}/{jobId}")
    public ResponseEntity<String> deleteSavedJob(
            @PathVariable Long employeeId,
            @PathVariable Long jobId
    ) {
        savedJobService.deleteSavedJob(employeeId, jobId);
        return ResponseEntity.ok("Job removed from saved list successfully");
    }



}

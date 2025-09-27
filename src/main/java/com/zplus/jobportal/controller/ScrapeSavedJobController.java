package com.zplus.jobportal.controller;

import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.model.ScrapeSavedJobs;
import com.zplus.jobportal.services.ScrapeSavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/scrape-saved-jobs")
@RequiredArgsConstructor
public class ScrapeSavedJobController {

    private final ScrapeSavedJobService service;

    @PostMapping("/{employeeId}")
    public ResponseEntity<ScrapeSavedJobs> saveJob(
            @PathVariable Long employeeId,
            @RequestBody Job job) {
        return ResponseEntity.ok(service.saveJobForEmployee(employeeId, job));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<ScrapeSavedJobs>> getSavedJobs(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.getJobsForEmployee(employeeId));
    }

    @DeleteMapping("/{employeeId}/{jobId}")
    public ResponseEntity<String> deleteSavedJob(
            @PathVariable Long employeeId,
            @PathVariable Long jobId
            ) {
        service.deleteSavedJob(employeeId, jobId);
        return ResponseEntity.ok("Job removed from saved list successfully");
    }
}

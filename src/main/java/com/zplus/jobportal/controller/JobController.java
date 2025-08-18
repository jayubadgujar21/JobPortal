package com.zplus.jobportal.controller;

import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.services.JobServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobServices jobService;

    @PostMapping("/add")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/getAllJobs/{employeeId}")
    public ResponseEntity<List<Job>> getAllJobs(@PathVariable Long employeeId) {
        return ResponseEntity.ok(jobService.getAllJobs(employeeId));
    }

    @GetMapping("/getJob/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) throws Throwable {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping("/findJobByTitle/{jobTitle}")
    public ResponseEntity<List<Job>> getJobByTitle(@RequestParam String jobTitle){
        return ResponseEntity.ok(jobService.findJobByTitle(jobTitle));
    }

} 
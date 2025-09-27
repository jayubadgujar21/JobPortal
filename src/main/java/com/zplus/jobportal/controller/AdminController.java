package com.zplus.jobportal.controller;

import com.zplus.jobportal.dto.request.AdminLoginRequest;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.repository.JobRepo;
import com.zplus.jobportal.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final JobRepo jobRepo;

    public AdminController(AdminService adminService, JobRepo jobRepo) {
        this.adminService = adminService;
        this.jobRepo = jobRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AdminLoginRequest request) {
        return adminService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(adminService.logout());
    }

    @GetMapping("/getAllJobs")
    public ResponseEntity<List<Job>> getAllJobs(){
        return ResponseEntity.ok(jobRepo.findAll());
    }
}

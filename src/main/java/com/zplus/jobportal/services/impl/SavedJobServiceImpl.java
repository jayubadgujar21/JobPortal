package com.zplus.jobportal.services.impl;

import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.model.SavedJob;
import com.zplus.jobportal.repository.EmployeeRepo;
import com.zplus.jobportal.repository.JobRepo;
import com.zplus.jobportal.repository.SavedJobRepository;
import com.zplus.jobportal.services.SavedJobService;
import org.springframework.stereotype.Service;


@Service
public class SavedJobServiceImpl implements SavedJobService {
    private final SavedJobRepository savedJobRepository;
    private final EmployeeRepo employeeRepo;
    private final JobRepo jobRepo;

    public SavedJobServiceImpl(SavedJobRepository savedJobRepository, EmployeeRepo employeeRepo, JobRepo jobRepo) {
        this.savedJobRepository = savedJobRepository;
        this.employeeRepo = employeeRepo;
        this.jobRepo = jobRepo;
    }


    @Override
    public SavedJob saveJob(Long employeeId, Long jobId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Check if already saved
        savedJobRepository.findByEmployeeAndJob(employee, job)
                .ifPresent(s -> {
                    throw new RuntimeException("Job already saved by this employee");
                });

        SavedJob savedJob = new SavedJob();
        savedJob.setEmployee(employee);
        savedJob.setJob(job);

        return savedJobRepository.save(savedJob);
    }

    @Override
    public void deleteSavedJob(Long employeeId, Long jobId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        SavedJob savedJob = savedJobRepository.findByEmployeeAndJob(employee, job)
                .orElseThrow(() -> new RuntimeException("Saved job not found"));

        savedJobRepository.delete(savedJob);
    }
}

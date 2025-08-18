package com.zplus.jobportal.services.impl;

import com.zplus.jobportal.Exception.ApiError;
import com.zplus.jobportal.dto.response.SavedJobResponse;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.model.SavedJob;
import com.zplus.jobportal.repository.EmployeeRepo;
import com.zplus.jobportal.repository.JobRepo;
import com.zplus.jobportal.repository.SavedJobRepository;
import com.zplus.jobportal.services.SavedJobService;
import org.springframework.stereotype.Service;
import java.util.List;


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
                .orElseThrow(() -> new ApiError(404,"Employee not found with id: " + employeeId));

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new ApiError(404,"Job not found with id"+jobId));

        // Check if already saved
        savedJobRepository.findByEmployeeAndJob(employee, job)
                .ifPresent(s -> {
                    throw new ApiError(402,"Job already saved by employee");
                });

        SavedJob savedJob = new SavedJob();
        savedJob.setEmployee(employee);
        savedJob.setJob(job);

        return savedJobRepository.save(savedJob);
    }

    @Override
    public void deleteSavedJob(Long employeeId, Long jobId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ApiError(404,"Employee not found with id: " + employeeId));

        Job job = jobRepo.findById(jobId)
                .orElseThrow(() -> new ApiError(404,"Job not found with id"+jobId));

        SavedJob savedJob = savedJobRepository.findByEmployeeAndJob(employee, job)
                .orElseThrow(() -> new ApiError(404,"Saved job not found"));

        savedJobRepository.delete(savedJob);
    }

    @Override
    public List<Job> findSavedJobById(Long employeeId) {
        List<SavedJob> savedJobs = savedJobRepository.findByEmployeeId(employeeId);

        return savedJobs.stream()
                .map(SavedJob::getJob)   // extract Job from each SavedJob
                .toList();
    }
}

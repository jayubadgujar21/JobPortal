package com.zplus.jobportal.services;

import com.zplus.jobportal.Exception.ApiError;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.model.SavedJob;
import com.zplus.jobportal.model.ScrapeSavedJobs;
import com.zplus.jobportal.repository.EmployeeRepo;
import com.zplus.jobportal.repository.ScrapeSavedJobRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScrapeSavedJobService {
    private final ScrapeSavedJobRepo scrapeSavedJobRepo;
    private final EmployeeRepo employeeRepo;

    public ScrapeSavedJobService(ScrapeSavedJobRepo scrapeSavedJobRepo, EmployeeRepo employeeRepo) {
        this.scrapeSavedJobRepo = scrapeSavedJobRepo;
        this.employeeRepo = employeeRepo;
    }

    public ScrapeSavedJobs saveJobForEmployee(Long employeeId, Job job) {
        ScrapeSavedJobs saved = new ScrapeSavedJobs();
        saved.setEmployeeId(employeeId);
        saved.setTitle(job.getJobTitle());
        saved.setCompany(job.getCompany());
        saved.setLocation(job.getLocation());
        saved.setExperience(job.getExperience());
        saved.setPlatform(job.getPlatform());
        saved.setApplyLink(job.getApplyLink());
        return scrapeSavedJobRepo.save(saved);
    }

    public List<ScrapeSavedJobs> getJobsForEmployee(Long employeeId) {
        return scrapeSavedJobRepo.findByEmployeeId(employeeId);
    }

//    public String deleteSavedJob(Long employeeId,Job job){
//        return scrapeSavedJobRepo.findByEmployeeIdAndApplyLink(employeeId, job.getApplyLink())
//                .map(savedJob -> {
//                    scrapeSavedJobRepo.delete(savedJob);
//                    return "Job deleted successfully";
//                })
//                .orElse("Job not found for this employee");
//
//        public void deleteSavedJob(Long employeeId, Long jobId) {
//            Employee employee = employeeRepo.findById(employeeId)
//                    .orElseThrow(() -> new ApiError(404,"Employee not found with id: " + employeeId));
//
//            Job job = jobRepo.findById(jobId)
//                    .orElseThrow(() -> new ApiError(404,"Job not found with id"+jobId));
//
//            SavedJob savedJob = savedJobRepository.findByEmployeeAndJob(employee, job)
//                    .orElseThrow(() -> new ApiError(404,"Saved job not found"));
//
//            savedJobRepository.delete(savedJob);
//        }
//    }

    public String deleteSavedJob(Long employeeId, Long jobId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ApiError(404,"Employee not found with id: " + employeeId));

        ScrapeSavedJobs job = scrapeSavedJobRepo.findById(jobId)
                .orElseThrow(() -> new ApiError(404,"Job not found with id"+jobId));

        scrapeSavedJobRepo.delete(job);
        return "Job Removed";
    }
}

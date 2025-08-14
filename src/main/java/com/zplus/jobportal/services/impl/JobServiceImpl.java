package com.zplus.jobportal.services.impl;


import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.repository.JobRepo;
import com.zplus.jobportal.services.JobServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobServiceImpl implements JobServices {

    @Autowired
    private JobRepo jobRepository;

    @Override
    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    @Override
    public Job updateJob(Long id, Job job) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        existingJob.setJobTitle(job.getJobTitle());
        existingJob.setCompany(job.getCompany());
        existingJob.setLocation(job.getLocation());
        existingJob.setExperience(job.getExperience());
        existingJob.setApplyLink(job.getApplyLink());

        return jobRepository.save(existingJob);
    }

    @Override
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found");
        }
        jobRepository.deleteById(id);
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public Job getJobById(Long id) throws Throwable {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    @Override
    public List<Job> findJobByTitle(String jobTitle){
        List<Job> jobs=jobRepository.findByJobTitleContainingIgnoreCase(jobTitle);
        if(jobs.isEmpty()){
            throw new RuntimeException("No jobs found");
        }
        return jobs;
    }
}


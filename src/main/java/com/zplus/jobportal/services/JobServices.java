package com.zplus.jobportal.services;

import com.zplus.jobportal.model.Job;

import java.util.List;

public interface JobServices {
    Job createJob(Job job);
    Job updateJob(Long id, Job job);
    void deleteJob(Long id);
    List<Job> getAllJobs();
    Job getJobById(Long id) throws Throwable;
    List<Job> findJobByTitle(String jobTitle);
}

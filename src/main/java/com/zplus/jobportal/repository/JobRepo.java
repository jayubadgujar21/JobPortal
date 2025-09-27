package com.zplus.jobportal.repository;

import com.zplus.jobportal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepo extends JpaRepository<Job,Long> {
    // Case-insensitive partial match
    List<Job> findByJobTitleContainingIgnoreCase(String jobTitle);
}

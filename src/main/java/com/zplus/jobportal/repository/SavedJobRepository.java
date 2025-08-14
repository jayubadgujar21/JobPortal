package com.zplus.jobportal.repository;

import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByEmployee(Employee student);
    Optional<SavedJob> findByEmployeeAndJob(Employee employee, Job job);
}

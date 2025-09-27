package com.zplus.jobportal.repository;

import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.model.Job;
import com.zplus.jobportal.model.SavedJob;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface
SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByEmployee(Employee student);
    Optional<SavedJob> findByEmployeeAndJob(Employee employee, Job job);
    List<SavedJob> findByEmployeeId(Long employeeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SavedJob s WHERE s.job.id = :jobId")
    void deleteByJobId(Long jobId);
}

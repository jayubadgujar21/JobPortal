package com.zplus.jobportal.repository;

import com.zplus.jobportal.model.ScrapeSavedJobs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapeSavedJobRepo extends JpaRepository<ScrapeSavedJobs,Long> {
    List<ScrapeSavedJobs> findByEmployeeId(Long employeeId);
    Optional<ScrapeSavedJobs> findByEmployeeIdAndApplyLink(Long employeeId, String redirectUrl);
}

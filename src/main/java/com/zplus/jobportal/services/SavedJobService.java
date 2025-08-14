package com.zplus.jobportal.services;

import com.zplus.jobportal.model.SavedJob;

public interface SavedJobService {
    SavedJob saveJob(Long employeeId, Long jobId);
    public void deleteSavedJob(Long employeeId, Long jobId);
}

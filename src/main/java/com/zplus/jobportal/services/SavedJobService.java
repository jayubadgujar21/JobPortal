package com.zplus.jobportal.services;

import com.zplus.jobportal.model.SavedJob;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public interface SavedJobService {
    SavedJob saveJob(Long employeeId, Long jobId);
    public void deleteSavedJob(Long employeeId, Long jobId);
    List<SavedJob> findSavedJobById(Long employeeId);
}

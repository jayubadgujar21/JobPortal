package com.zplus.jobportal.dto.response;

import com.zplus.jobportal.model.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavedJobResponse {
    private Long savedJobId;
    private Job job;
}

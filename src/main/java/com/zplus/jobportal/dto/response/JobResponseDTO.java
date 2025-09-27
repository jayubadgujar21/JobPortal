package com.zplus.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResponseDTO {
    private String jobTitle;
    private String company;
    private String location;
    private String applyLink;
    private String experience;
    private String platform;
}

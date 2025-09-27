package com.zplus.jobportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scrapped_saved_jobs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapeSavedJobs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId; // Who saved the job

    private String title;
    private String company;
    private String location;
    private String experience;
    private String applyLink;
    private String platform;

}


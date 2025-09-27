package com.zplus.jobportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobTitle;
    private String company;
    private String location;
    private String experience;
    private String applyLink;
    private String platform;


    public Job(String jobTitle, String company, String location, String experience,String applyLink, String platform) {
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.experience = experience;
        this.applyLink = applyLink;
        this.platform = platform;
    }

}
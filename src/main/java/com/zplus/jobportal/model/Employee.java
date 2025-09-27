package com.zplus.jobportal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("FullName")
    @Column(nullable = false)
    private String fullName;

    @JsonProperty("email")
    @Column(unique = true)
    private String email;

    @JsonProperty("password")
    @Column(nullable = false)
    private String password;

    @JsonProperty("age")
    @Column(nullable = false)
    private int age;

    @JsonProperty("MobileNo")
    @Column(nullable = false)
    private String mobileNo;

    @JsonProperty("designation")
    @Column(nullable = false)
    private String designation;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SavedJob> savedJobs = new ArrayList<>();

    private boolean paymentDone = false;

    private LocalDate paymentExpiryDate = null;

}
package com.zplus.jobportal.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDto {
    private Long id;
    private String fullName;
    private String email;
    private int age;
    private String mobileNo;
    private String designation;
    private boolean paymentDone;
    private LocalDate paymentExpiryDate;
    private long daysRemaining;
    private boolean expiringSoon;
    private boolean expired;


}

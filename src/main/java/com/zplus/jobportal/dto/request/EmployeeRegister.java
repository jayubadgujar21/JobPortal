package com.zplus.jobportal.dto.request;

import lombok.Data;

@Data
public class EmployeeRegister {
        private String fullName;
        private String email;
        private String password;
        private int age;
        private String mobileNo;
        private String designation;

}

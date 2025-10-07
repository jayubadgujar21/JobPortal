package com.zplus.jobportal.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String email;
    private String newPassword;
    private String confirmPassword;
}

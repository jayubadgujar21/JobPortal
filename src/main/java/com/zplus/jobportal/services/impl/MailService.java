package com.zplus.jobportal.services.impl;

import com.zplus.jobportal.Exception.ApiError;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepo employeeRepo;

    public void sendEmailToResetPassword(String toEmail,String otp){
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Password Reset Request");
        msg.setText("Copy the below OTP and paste it : \n\n"+ otp);
//        msg.setReplyTo("scoopen@gmail.com");
        mailSender.send(msg);
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public String verifyOtp(String email, String otp) {
        Employee employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new ApiError(404, "User not found"));

        if (employee.getOtp() == null || employee.getOtpExpiry() == null) {
            throw new ApiError(400, "No OTP requested");
        }

        if (employee.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiError(400, "OTP expired");
        }

        if (!employee.getOtp().equals(otp)) {
            throw new ApiError(400, "Invalid OTP");
        }

        // ✅ OTP verified
        return "OTP verified, you can reset your password";
    }

}

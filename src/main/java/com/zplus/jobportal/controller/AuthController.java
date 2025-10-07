package com.zplus.jobportal.controller;

import com.zplus.jobportal.Exception.ApiError;
import com.zplus.jobportal.dto.request.*;
import com.zplus.jobportal.dto.response.EmployeeDto;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.services.EmployeeService;
import com.zplus.jobportal.services.impl.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmployeeService employeeService;
    private final MailService mailService;

    @Autowired
    public AuthController(EmployeeService employeeService, MailService mailService) {
        this.employeeService = employeeService;
        this.mailService = mailService;
    }

    @PostMapping("/register")
    public ResponseEntity<Employee> registerUser(@RequestBody EmployeeRegister employee) {
        return ResponseEntity.ok(employeeService.registerNewUser(employee));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody EmployeeLoginReq dto) {
        Employee employee = employeeService.loginUser(dto);
        employeeService.updatePaymentStatusIfExpired(employee);
        EmployeeDto employeeDto = employeeService.mapToDto(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("employee", employeeDto);

        if (!employee.isPaymentDone()) {
            response.put("message", "Your subscription/payment has expired. Please renew to access the job portal.");
        }
        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotUserPassword(@RequestBody ForgotPasswordRequestDTO dto){
        String msg = employeeService.forgotPassword(dto.getEmail());
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        String response = mailService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDTO dto){
        return ResponseEntity.ok(employeeService.resetPassword(dto.getEmail(),dto.getNewPassword(),dto.getConfirmPassword()));
    }

} 
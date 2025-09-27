package com.zplus.jobportal.controller;

import com.zplus.jobportal.Exception.ApiError;
import com.zplus.jobportal.dto.request.EmployeeLoginReq;
import com.zplus.jobportal.dto.request.EmployeeRegister;
import com.zplus.jobportal.dto.response.EmployeeDto;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.services.EmployeeService;
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

    @Autowired
    public AuthController( EmployeeService employeeService) {
        this.employeeService = employeeService;
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




} 
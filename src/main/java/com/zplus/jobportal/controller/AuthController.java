package com.zplus.jobportal.controller;

import com.zplus.jobportal.dto.request.EmployeeLoginReq;
import com.zplus.jobportal.dto.request.EmployeeRegister;
import com.zplus.jobportal.dto.response.EmployeeDto;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<EmployeeDto> login(@RequestBody EmployeeLoginReq dto) {
        Employee employee = employeeService.loginUser(dto);
        EmployeeDto employeeDto = employeeService.mapToDto(employee);
        return ResponseEntity.ok(employeeDto);
    }


} 
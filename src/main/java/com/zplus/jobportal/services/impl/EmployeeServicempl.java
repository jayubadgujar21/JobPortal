package com.zplus.jobportal.services.impl;

import com.zplus.jobportal.Exception.ApiError;
import com.zplus.jobportal.dto.request.EmployeeLoginReq;
import com.zplus.jobportal.dto.request.EmployeeRegister;
import com.zplus.jobportal.dto.response.EmployeeDto;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.repository.EmployeeRepo;
import com.zplus.jobportal.services.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServicempl implements EmployeeService {

    private final EmployeeRepo employeeRepository;

    public EmployeeServicempl(EmployeeRepo employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    @Override
    public Employee registerNewUser(EmployeeRegister dto) {
        // Check if email already exists
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ApiError(401,"Email already registered");
        }
        // Map DTO to Entity
        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPassword(dto.getPassword());
        employee.setAge(dto.getAge());
        employee.setMobileNo(dto.getMobileNo());
        employee.setDesignation(dto.getDesignation());
        employee.setPaymentDone(false);
        employee.setPaymentExpiryDate(null);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee loginUser(EmployeeLoginReq dto) {
        Employee employee = employeeRepository.findByEmail(dto.getEmail()).orElseThrow(()-> new ApiError(404,"Employee not found with email "+dto.getEmail()));
        if(!employee.getPassword().equals(dto.getPassword())){
            throw new ApiError(401,"Incorrect Password");
        }
        return employee;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Employee updateEmployee(Long id, EmployeeRegister employee) {
        return employeeRepository.findById(id)
                .map(existing -> {
                    existing.setFullName(employee.getFullName());
                    existing.setEmail(employee.getEmail());
                    existing.setPassword(employee.getPassword());
                    existing.setAge(employee.getAge());
                    existing.setMobileNo(employee.getMobileNo());
                    existing.setDesignation(employee.getDesignation());
                    return employeeRepository.save(existing);
                })
                .orElseThrow(() -> new ApiError(404,"Employee not found with id "+ id));
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public  EmployeeDto mapToDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        dto.setAge(employee.getAge());
        dto.setMobileNo(employee.getMobileNo());
        dto.setDesignation(employee.getDesignation());
        dto.setPaymentDone(employee.isPaymentDone());
        dto.setPaymentExpiryDate(employee.getPaymentExpiryDate());

        long daysRemaining = 0;
        boolean isExpiringSoon = false;

        if (employee.getPaymentExpiryDate() != null) {
            // Hardcoded current date for testing
            LocalDate today = LocalDate.of(2025, 9, 17);
            daysRemaining = ChronoUnit.DAYS.between(today, employee.getPaymentExpiryDate());

            if (daysRemaining > 0 && daysRemaining <= 5) {
                isExpiringSoon = true;
            } else {
                daysRemaining = 0; // Reset if not in the 5-day window
            }
        }

        dto.setDaysRemaining(daysRemaining);
        dto.setExpiringSoon(isExpiringSoon);

        return dto;
    }

}


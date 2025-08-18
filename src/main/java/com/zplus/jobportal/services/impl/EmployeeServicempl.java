package com.zplus.jobportal.services.impl;

import com.zplus.jobportal.dto.request.EmployeeLoginReq;
import com.zplus.jobportal.dto.request.EmployeeRegister;
import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.repository.EmployeeRepo;
import com.zplus.jobportal.services.EmployeeService;
import org.springframework.stereotype.Service;

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
            throw new RuntimeException("Email already registered");
        }
        // Map DTO to Entity
        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPassword(dto.getPassword());
        employee.setAge(dto.getAge());
        employee.setMobileNo(dto.getMobileNo());
        employee.setDesignation(dto.getDesignation());

        return employeeRepository.save(employee);
    }

    @Override
    public Employee loginUser(EmployeeLoginReq dto) {
        Employee employee = employeeRepository.findByEmail(dto.getEmail()).orElseThrow(()-> new RuntimeException("Employee not found with email "+dto.getEmail()));
        if(!employee.getPassword().equals(dto.getPassword())){
            throw new RuntimeException("Incorrect Password");
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
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + id));
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}

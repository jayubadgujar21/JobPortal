package com.zplus.jobportal.services;

import com.zplus.jobportal.dto.request.EmployeeLoginReq;
import com.zplus.jobportal.dto.request.EmployeeRegister;
import com.zplus.jobportal.model.Employee;

import java.util.List;
import java.util.Optional;


public interface EmployeeService {
    Employee registerNewUser(EmployeeRegister dto);
    Employee loginUser(EmployeeLoginReq dto);
    //Employee createEmployee(Employee employee);
    List<Employee> getAllEmployees();
    Optional<Employee> getEmployeeById(Long id);
    Employee updateEmployee(Long id, EmployeeRegister dto);
    void deleteEmployee(Long id);
}

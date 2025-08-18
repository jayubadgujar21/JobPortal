package com.zplus.jobportal.repository;

import com.zplus.jobportal.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee,Long> {
    List<Employee> findByPaymentDoneTrueAndPaymentExpiryDateBefore(LocalDate date);
    Optional<Employee> findByEmail(String email);
}

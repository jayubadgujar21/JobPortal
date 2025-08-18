package com.zplus.jobportal.controller;


import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.repository.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final EmployeeRepo employeeRepository;

    @PostMapping("/success/{employeeId}")
    public ResponseEntity<String> paymentSuccess(@PathVariable Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID "+employeeId));

        employee.setPaymentDone(true);
        employee.setPaymentExpiryDate(LocalDate.now().plusDays(30));

        employeeRepository.save(employee);

        return ResponseEntity.ok("Payment marked as successful. Access valid for 30 days.");
    }
}


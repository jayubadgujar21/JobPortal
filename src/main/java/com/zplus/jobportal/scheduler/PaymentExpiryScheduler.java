package com.zplus.jobportal.scheduler;

import com.zplus.jobportal.model.Employee;
import com.zplus.jobportal.repository.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class PaymentExpiryScheduler {

    private final EmployeeRepo employeeRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Runs every midnight
    public void checkAndExpirePayments() {
        List<Employee> expiredEmployees = employeeRepository
                .findByPaymentDoneTrueAndPaymentExpiryDateBefore(LocalDate.now());

        for (Employee emp : expiredEmployees) {
            emp.setPaymentDone(false);
        }

        if (!expiredEmployees.isEmpty()) {
            employeeRepository.saveAll(expiredEmployees);
            System.out.println("Expired payments for " + expiredEmployees.size() + " employees");
        }
    }
}


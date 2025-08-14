package com.zplus.jobportal.services;


import com.zplus.jobportal.dto.request.AdminLoginRequest;
//import com.zplus.jobportal.dto.AdminLoginResponse;
import com.zplus.jobportal.model.Admin;
import com.zplus.jobportal.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public ResponseEntity<String> login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!request.getPassword().equals( admin.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return ResponseEntity.ok("Admin login successfully..!!");
    }

    public String logout() {
        // With JWT, logout is usually handled on client side
        return "Logout successful";
    }
}


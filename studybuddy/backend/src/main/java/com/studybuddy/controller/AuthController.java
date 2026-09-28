package com.studybuddy.controller;

import com.studybuddy.dto.ApiResponse;
import com.studybuddy.dto.AuthResponse;
import com.studybuddy.dto.LoginRequest;
import com.studybuddy.dto.StudentResponse;
import com.studybuddy.entity.Student;
import com.studybuddy.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentResponse>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken) {

        String token = authHeader != null ? authHeader : xAuthToken;
        Student student = authService.getAuthenticatedStudent(token);
        return ResponseEntity.ok(ApiResponse.success("Current user profile", StudentResponse.fromEntity(student)));
    }
}

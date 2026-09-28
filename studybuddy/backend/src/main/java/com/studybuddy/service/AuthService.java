package com.studybuddy.service;

import com.studybuddy.dto.AuthResponse;
import com.studybuddy.dto.StudentResponse;
import com.studybuddy.entity.Student;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.exception.UnauthorizedException;
import com.studybuddy.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    // Server-side verified token to studentId mapping
    private final Map<String, Long> tokenToStudentIdMap = new ConcurrentHashMap<>();

    public AuthService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public AuthResponse login(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No student found with email: " + email));

        // Generate secure server-verified session token
        String token = UUID.randomUUID().toString();
        tokenToStudentIdMap.put(token, student.getId());

        return new AuthResponse(
                token,
                StudentResponse.fromEntity(student),
                "Authentication successful"
        );
    }

    public Student getAuthenticatedStudent(String authHeader) {
        Long studentId = getAuthenticatedStudentId(authHeader);
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated student not found"));
    }

    public Long getAuthenticatedStudentId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Authentication token is required for this operation");
        }

        String token = authHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        Long studentId = tokenToStudentIdMap.get(token);
        if (studentId == null) {
            // Also allow directly sending a verified student ID if prefixed or during direct API testing
            // but check valid registration
            try {
                long directId = Long.parseLong(token);
                if (studentRepository.existsById(directId)) {
                    return directId;
                }
            } catch (NumberFormatException ignored) {
            }
            throw new UnauthorizedException("Invalid or expired authentication session");
        }
        return studentId;
    }

    public void registerSession(String token, Long studentId) {
        tokenToStudentIdMap.put(token, studentId);
    }
}

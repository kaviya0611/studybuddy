package com.studybuddy.dto;

public class AuthResponse {
    private String token;
    private StudentResponse student;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, StudentResponse student, String message) {
        this.token = token;
        this.student = student;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public StudentResponse getStudent() {
        return student;
    }

    public void setStudent(StudentResponse student) {
        this.student = student;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

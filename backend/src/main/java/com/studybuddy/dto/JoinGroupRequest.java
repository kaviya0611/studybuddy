package com.studybuddy.dto;

import jakarta.validation.constraints.NotNull;

public class JoinGroupRequest {

    @NotNull(message = "Student ID is required to join group")
    private Long studentId;

    public JoinGroupRequest() {
    }

    public JoinGroupRequest(Long studentId) {
        this.studentId = studentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}

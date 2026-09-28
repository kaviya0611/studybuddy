package com.studybuddy.dto;

import com.studybuddy.entity.Subject;

public class SubjectResponse {
    private Long id;
    private String subjectName;
    private String subjectCode;

    public SubjectResponse() {
    }

    public SubjectResponse(Long id, String subjectName, String subjectCode) {
        this.id = id;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
    }

    public static SubjectResponse fromEntity(Subject subject) {
        if (subject == null) return null;
        return new SubjectResponse(subject.getId(), subject.getSubjectName(), subject.getSubjectCode());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
}

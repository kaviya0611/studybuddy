package com.studybuddy.dto;

import com.studybuddy.entity.StudyGroup;
import java.time.LocalDateTime;

public class StudyGroupResponse {
    private Long id;
    private String groupName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Long creatorId;
    private String creatorName;
    private String creatorEmail;
    private Integer maxMembers;
    private Long currentMemberCount;
    private Long availableSpaces;
    private LocalDateTime createdAt;

    public StudyGroupResponse() {
    }

    public StudyGroupResponse(Long id, String groupName, Long subjectId, String subjectName,
                              String subjectCode, Long creatorId, String creatorName,
                              String creatorEmail, Integer maxMembers, Long currentMemberCount,
                              Long availableSpaces, LocalDateTime createdAt) {
        this.id = id;
        this.groupName = groupName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.creatorEmail = creatorEmail;
        this.maxMembers = maxMembers;
        this.currentMemberCount = currentMemberCount;
        this.availableSpaces = availableSpaces;
        this.createdAt = createdAt;
    }

    public static StudyGroupResponse fromEntity(StudyGroup group, long currentMemberCount) {
        if (group == null) return null;
        long available = Math.max(0, group.getMaxMembers() - currentMemberCount);
        return new StudyGroupResponse(
                group.getId(),
                group.getGroupName(),
                group.getSubject() != null ? group.getSubject().getId() : null,
                group.getSubject() != null ? group.getSubject().getSubjectName() : null,
                group.getSubject() != null ? group.getSubject().getSubjectCode() : null,
                group.getCreator() != null ? group.getCreator().getId() : null,
                group.getCreator() != null ? group.getCreator().getName() : null,
                group.getCreator() != null ? group.getCreator().getEmail() : null,
                group.getMaxMembers(),
                currentMemberCount,
                available,
                group.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Long getCurrentMemberCount() {
        return currentMemberCount;
    }

    public void setCurrentMemberCount(Long currentMemberCount) {
        this.currentMemberCount = currentMemberCount;
    }

    public Long getAvailableSpaces() {
        return availableSpaces;
    }

    public void setAvailableSpaces(Long availableSpaces) {
        this.availableSpaces = availableSpaces;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

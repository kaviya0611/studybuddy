package com.studybuddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters")
    private String groupName;

    @NotNull(message = "Subject selection is required")
    private Long subjectId;

    @NotNull(message = "Student selection (Creator) is required")
    private Long creatorId;

    @NotNull(message = "Maximum member capacity is required")
    @Min(value = 1, message = "Maximum member capacity must be a positive number greater than 0")
    private Integer maxMembers;

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(String groupName, Long subjectId, Long creatorId, Integer maxMembers) {
        this.groupName = groupName;
        this.subjectId = subjectId;
        this.creatorId = creatorId;
        this.maxMembers = maxMembers;
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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
}

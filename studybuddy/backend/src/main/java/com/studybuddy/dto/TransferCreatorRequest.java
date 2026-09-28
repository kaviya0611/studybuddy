package com.studybuddy.dto;

import jakarta.validation.constraints.NotNull;

public class TransferCreatorRequest {

    @NotNull(message = "New creator student ID is required")
    private Long newCreatorId;

    public TransferCreatorRequest() {
    }

    public TransferCreatorRequest(Long newCreatorId) {
        this.newCreatorId = newCreatorId;
    }

    public Long getNewCreatorId() {
        return newCreatorId;
    }

    public void setNewCreatorId(Long newCreatorId) {
        this.newCreatorId = newCreatorId;
    }
}

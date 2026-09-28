package com.studybuddy.exception;

public class GroupFullException extends RuntimeException {
    public GroupFullException(String message) {
        super(message);
    }
}

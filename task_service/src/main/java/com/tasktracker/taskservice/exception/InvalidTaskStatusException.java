package com.tasktracker.taskservice.exception;

public class InvalidTaskStatusException extends RuntimeException {

    public InvalidTaskStatusException(String message) {
        super(message);
    }
}
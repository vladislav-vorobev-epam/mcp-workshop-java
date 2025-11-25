package com.epam.masterclass.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Task not found with id: " + taskId);
    }
}

package com.epam.masterclass.exception;

import com.epam.masterclass.model.TaskStatus;

import java.util.Set;
import java.util.stream.Collectors;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TaskStatus currentStatus, TaskStatus targetStatus) {
        super(buildMessage(currentStatus, targetStatus));
    }

    private static String buildMessage(TaskStatus currentStatus, TaskStatus targetStatus) {
        if (currentStatus == TaskStatus.DONE) {
            return String.format(
                    "Cannot change status of completed task. Current status: %s",
                    currentStatus
            );
        }

        Set<TaskStatus> allowedTransitions = currentStatus.getAllowedTransitions();
        String allowedList = allowedTransitions.stream()
                .map(status -> currentStatus + " → " + status)
                .collect(Collectors.joining(", "));

        return String.format(
                "Invalid status transition from %s to %s. Allowed transitions: %s",
                currentStatus,
                targetStatus,
                allowedList
        );
    }
}

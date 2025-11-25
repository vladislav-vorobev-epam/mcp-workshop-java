package com.epam.masterclass.model;

import java.util.Set;

public enum TaskStatus {
    NEW,
    IN_PROGRESS,
    DONE;

    /**
     * Checks if transition from current status to target status is allowed.
     *
     * @param targetStatus the desired status to transition to
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransitionTo(TaskStatus targetStatus) {
        return switch (this) {
            case NEW -> targetStatus == IN_PROGRESS;
            case IN_PROGRESS -> targetStatus == DONE || targetStatus == NEW;
            case DONE -> false; // Terminal state
        };
    }

    /**
     * Gets all allowed transitions from current status.
     *
     * @return set of allowed target statuses
     */
    public Set<TaskStatus> getAllowedTransitions() {
        return switch (this) {
            case NEW -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(NEW, DONE);
            case DONE -> Set.of();
        };
    }
}

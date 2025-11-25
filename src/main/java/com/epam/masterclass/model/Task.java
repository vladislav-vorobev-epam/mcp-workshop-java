package com.epam.masterclass.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Task(
        @JsonProperty("id")
        String id,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("status")
        TaskStatus status,

        @JsonProperty("createdAt")
        Instant createdAt,

        @JsonProperty("updatedAt")
        Instant updatedAt
) {
    /**
     * Creates a new task from creation request.
     *
     * @param title       task title
     * @param description task description
     * @return newly created task with NEW status
     */
    public static Task create(String title, String description) {
        Instant now = Instant.now();
        return new Task(
                UUID.randomUUID().toString(),
                title,
                description,
                TaskStatus.NEW,
                now,
                now
        );
    }

    /**
     * Creates a copy of this task with updated status.
     *
     * @param newStatus the new status to set
     * @return new task instance with updated status and updatedAt timestamp
     */
    public Task withStatus(TaskStatus newStatus) {
        return new Task(
                this.id,
                this.title,
                this.description,
                newStatus,
                this.createdAt,
                Instant.now()
        );
    }
}

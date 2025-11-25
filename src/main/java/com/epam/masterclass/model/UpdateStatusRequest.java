package com.epam.masterclass.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @JsonProperty("status")
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}

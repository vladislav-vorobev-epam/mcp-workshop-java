package com.epam.masterclass.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @JsonProperty("title")
        @NotBlank(message = "Title is required and cannot be blank")
        @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
        String title,

        @JsonProperty("description")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}

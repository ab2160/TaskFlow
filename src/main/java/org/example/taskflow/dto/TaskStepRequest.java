package org.example.taskflow.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskStepRequest {
    @NotBlank(message = "Step description is required")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

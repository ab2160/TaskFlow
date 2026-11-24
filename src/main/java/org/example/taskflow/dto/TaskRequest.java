package org.example.taskflow.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import org.example.taskflow.enums.TaskPriority;
import org.example.taskflow.enums.TaskStatus;

import java.time.LocalDateTime;

public class TaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    private String assignedUsername;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getAssignedUsername() {
        return assignedUsername;
    }

    public void setAssignedUsername(String assignedUsername) {
        this.assignedUsername = assignedUsername;
    }
}

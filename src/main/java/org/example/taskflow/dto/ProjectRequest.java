package org.example.taskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectRequest {
    private String name;
    private String description;
    private String status;
    private LocalDateTime deadline;


    public ProjectRequest() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}

package org.example.taskflow.controller;

import org.example.taskflow.dto.TaskRequest;
import org.example.taskflow.entity.Task;
import org.example.taskflow.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Task> createTask(@PathVariable Long projectId, @RequestBody TaskRequest request){
        return ResponseEntity.ok(service.createTask(projectId, request));
    }
    @GetMapping
    public ResponseEntity<List<Task>> getTasks(@PathVariable Long projectId){
        return ResponseEntity.ok(service.getProjectTasks(projectId));
    }
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long projectId, @PathVariable Long taskId){
        return ResponseEntity.ok(service.getById(projectId, taskId));
    }
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody TaskRequest request){
        return ResponseEntity.ok(service.updateTask(taskId, request));
    }
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId){
        service.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}

package org.example.taskflow.controller;

import jakarta.validation.Valid;
import org.example.taskflow.dto.TaskStepRequest;
import org.example.taskflow.entity.TaskStep;
import org.example.taskflow.service.TaskStepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskStepController {
    private final TaskStepService service;

    public TaskStepController(TaskStepService service) {
        this.service = service;
    }

    @PostMapping("/tasks/{taskId}/steps")
    public ResponseEntity<TaskStep> addStep(@Valid @PathVariable Long taskId, @RequestBody TaskStepRequest request){
        return ResponseEntity.ok(service.addStep(taskId, request));
    }

    @GetMapping("/tasks/{taskId}/steps")
    public ResponseEntity<List<TaskStep>> getTaskSteps(@PathVariable Long taskId){
        return ResponseEntity.ok(service.getTaskSteps(taskId));
    }

    @PutMapping("/steps/{stepId}")
    public ResponseEntity<TaskStep> updateStep(@PathVariable Long stepId, @RequestBody TaskStepRequest request){
        return ResponseEntity.ok(service.updateStep(stepId, request));
    }

    @PutMapping("/steps/{stepId}/complete")
    public ResponseEntity<TaskStep> markStepCompleted(@PathVariable Long stepId){
        return ResponseEntity.ok(service.markStepCompleted(stepId));
    }

    @PutMapping("/steps/{stepId}/uncomplete")
    public ResponseEntity<TaskStep> uncompleteStep(@PathVariable Long stepId){
        return ResponseEntity.ok(service.uncompleteStep(stepId));
    }

    @GetMapping("/tasks/{taskId}/progress")
    public ResponseEntity<Double> getTaskProgress(@PathVariable Long taskId){
        return ResponseEntity.ok(service.getTaskProgress(taskId));
    }

    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable Long stepId){
        service.deleteStep(stepId);
        return ResponseEntity.noContent().build();
    }
}

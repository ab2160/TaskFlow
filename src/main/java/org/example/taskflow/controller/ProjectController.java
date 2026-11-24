package org.example.taskflow.controller;

import jakarta.validation.Valid;
import org.example.taskflow.entity.Project;
import org.example.taskflow.service.ProjectService;
import org.example.taskflow.dto.ProjectRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request){
        return ResponseEntity.status(201).body(service.createProject(request));
    }
    @GetMapping
    public List<Project> getAllProjects(){
        return service.getAllProjects();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id){
        Optional<Project> project = service.getProjectById(id);
        return project.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request){
        Optional<Project> project = service.updateProject(id, request);
        return project.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id){
        boolean deleted = service.deleteProject(id);
        if(deleted){
            return ResponseEntity.ok("Project deleted successfully");
        }
        return ResponseEntity.status(404).body("Project not found");
    }
}

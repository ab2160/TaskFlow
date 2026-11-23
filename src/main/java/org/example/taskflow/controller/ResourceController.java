package org.example.taskflow.controller;

import org.example.taskflow.entity.Resource;
import org.example.taskflow.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ResourceController {
    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @PostMapping("/projects/{projectId}/resources")
    public ResponseEntity<Resource> uploadResources(@PathVariable Long projectId, @RequestParam("file")MultipartFile file)
            throws IOException{
        return ResponseEntity.ok(service.uploadResource(projectId, file));
    }

    @GetMapping("/projects/{projectId}/resources")
    public ResponseEntity<List<Resource>> getProjectResources(@PathVariable Long projectId){
        return ResponseEntity.ok(service.getProjectResources(projectId));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id){
        return ResponseEntity.ok(service.getResourceByID(id));
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) throws IOException{
        service.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}

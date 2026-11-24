package org.example.taskflow.controller;

import org.example.taskflow.entity.Resource;
import org.example.taskflow.service.ResourceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        return ResponseEntity.ok(service.getResourceById(id));
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) throws IOException{
        service.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resources/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Long id) throws IOException{
        Resource resource = service.getResourceById(id);
        org.springframework.core.io.Resource file = service.downloadResource(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getOriginalFileName() + "\"").body(file);
    }
}

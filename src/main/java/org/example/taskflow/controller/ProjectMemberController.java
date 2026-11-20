package org.example.taskflow.controller;

import org.example.taskflow.dto.AddProjectMemberRequest;
import org.example.taskflow.dto.ProjectMemberResponse;
import org.example.taskflow.entity.ProjectMember;
import org.example.taskflow.service.ProjectMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectMemberController {
    private final ProjectMemberService service;

    public ProjectMemberController(ProjectMemberService service) {
        this.service = service;
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMember> addMember(@PathVariable Long projectId, @RequestBody AddProjectMemberRequest request){
        return ResponseEntity.ok(service.addMember(projectId, request));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(@PathVariable Long projectId){
        return ResponseEntity.ok(service.getMembers(projectId));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId, @PathVariable Long userId){
        service.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}

package org.example.taskflow.service;

import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.ProjectMember;
import org.example.taskflow.entity.User;
import org.example.taskflow.repository.ProjectMemberRepository;
import org.example.taskflow.repository.ProjectRepository;
import org.example.taskflow.dto.ProjectRequest;
import org.example.taskflow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProjectService {
    private final UserRepository userRepository;
    private final ProjectRepository repository;
    private  final ProjectMemberRepository projectMemberRepository;

    public ProjectService(UserRepository userRepository, ProjectRepository repository, ProjectMemberRepository projectMemberRepository) {
        this.userRepository = userRepository;
        this.repository = repository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public Project createProject(ProjectRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByEmail(email);

        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser);
        project.setStatus(request.getStatus());
        project.setDeadline(request.getDeadline());
        if(request.getDeadline() != null && request.getDeadline().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dead line cannot be in the past");
        }
        project.setCreatedAt(LocalDateTime.now());

        return repository.save(project);
    }

    public List<Project> getMyProjects(){
        User currentUser = getCurrentUser();
        Set<Project> projects = new HashSet<>(repository.findByOwner(currentUser));
        List<ProjectMember> memberships = projectMemberRepository.findByUser(currentUser);

        for (ProjectMember membership : memberships){
            projects.add(membership.getProject());
        }
        return new ArrayList<>(projects);
    }


    public Optional<Project> getProjectById(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);

        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);

        if(!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this project");
        }
        return Optional.of(project);
    }

    public boolean deleteProject(Long id) {
        Project project = repository.findById(id).orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        ));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found"
            );
        }
        if (!project.getOwner().getId()
                .equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the project owner can delete this project"
            );
        }
        repository.delete(project);
        return true;
    }
    public Optional<Project> updateProject(Long id, ProjectRequest request) {
        Optional<Project> project = repository.findById(id);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);

        if (project.get().getOwner().getId().equals(currentUser.getId())) {
            project.get().setName(request.getName());
            project.get().setDescription(request.getDescription());
            project.get().setStatus(request.getStatus());
            project.get().setDeadline(request.getDeadline());

            return Optional.of(repository.save(project.get()));
        }
        if (!project.get().getOwner().getId().equals(currentUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the project owner can update this project"
            );
        }
        return Optional.empty();
    }
}

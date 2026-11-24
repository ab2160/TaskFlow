package org.example.taskflow.service;

import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.Resource;
import org.example.taskflow.entity.User;
import org.example.taskflow.enums.ResourceType;
import org.example.taskflow.repository.ProjectMemberRepository;
import org.example.taskflow.repository.ProjectRepository;
import org.example.taskflow.repository.ResourceRepository;
import org.example.taskflow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {
    private final String UPLOAD_DIR = "uploads";

    private final ResourceRepository resourceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ResourceService(ResourceRepository resourceRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository) {
        this.resourceRepository = resourceRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public Resource uploadResource(Long projectId, MultipartFile file) throws IOException{
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User currentUser = getCurrentUser();
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);
        if (!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }

        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String originalName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        Resource resource = new Resource();
        resource.setName(originalName);
        resource.setOriginalFileName(originalName);
        resource.setFilePath(filePath.toString());
        resource.setFileSize(file.getSize());
        resource.setUploadedAt(LocalDateTime.now());
        resource.setProject(project);
        resource.setUploadedBy(currentUser);

        String contentType = file.getContentType();
        if (contentType != null){
            if (contentType.startsWith("image")){
                resource.setType(ResourceType.IMAGE);
            }
            else if (contentType.startsWith("video")){
                resource.setType(ResourceType.VIDEO);
            }
            else if ("application/pdf".equals(contentType)){
                resource.setType(ResourceType.DOCUMENT);
            }
            else {
                resource.setType(ResourceType.FILE);
            }
        }

        return resourceRepository.save(resource);
    }

    public List<Resource> getProjectResources(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User currentUser = getCurrentUser();

        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);
        if (!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }

        return resourceRepository.findByProject(project);
    }

    public Resource getResourceById(Long id){
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        User currentUser = getCurrentUser();
        Project project = resource.getProject();
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);
        if (!isOwner && !isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }
        return resource;
    }

    public void deleteResource(Long id) throws IOException {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        User currentUser = getCurrentUser();

        boolean isOwner = resource.getProject().getOwner().getId().equals(currentUser.getId());
        boolean isUploader = resource.getUploadedBy().getId().equals(currentUser.getId());
        if (!isOwner && !isUploader){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete resource");
        }

        Files.deleteIfExists(Paths.get(resource.getFilePath()));
        resourceRepository.delete(resource);
    }

    public org.springframework.core.io.Resource downloadResource(Long id) throws IOException{
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        User currentUser = getCurrentUser();
        Project project = resource.getProject();

        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);
        if (!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }
        Path path = Paths.get(resource.getFilePath());
        org.springframework.core.io.Resource file = new UrlResource(path.toUri());
        if (!file.exists()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        return file;
    }
}

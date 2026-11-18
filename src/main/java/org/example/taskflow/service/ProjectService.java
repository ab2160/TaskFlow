package org.example.taskflow.service;

import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.User;
import org.example.taskflow.repository.ProjectRepository;
import org.example.taskflow.dto.ProjectRequest;
import org.example.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final UserRepository userRepository;
    private final ProjectRepository repository;

    public ProjectService(UserRepository userRepository, ProjectRepository repository) {
        this.userRepository = userRepository;
        this.repository = repository;
    }

    public String createProject(ProjectRequest request){
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(()-> new RuntimeException("User not found"));
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setStatus(request.getStatus());
        project.setDeadline(request.getDeadline());
        project.setCreatedAt(request.getCreatedAt());

        repository.save(project);
        return "Project created successfully.";
    }
    public List<Project> getAllProjects(){
        return repository.findAll();
    }
    public Optional<Project> getProjectById(Long id){
        return repository.findById(id);
    }
    public Optional<Project> updateProject(Long id, ProjectRequest request) {
       Optional <Project> project = repository.findById(id);

       if (project.isPresent()) {
           project.get().setName(request.getName());
           project.get().setDescription(request.getDescription());
           project.get().setStatus(request.getStatus());
           project.get().setDeadline(request.getDeadline());
           return Optional.of(repository.save(project.get()));
       }
       return Optional.empty();
    }
    public boolean deleteProject(Long id){
        if(repository.existsById(id)){
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}

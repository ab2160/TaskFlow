package org.example.taskflow.service;

import org.example.taskflow.dto.TaskRequest;
import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.Task;
import org.example.taskflow.entity.User;
import org.example.taskflow.repository.ProjectMemberRepository;
import org.example.taskflow.repository.ProjectRepository;
import org.example.taskflow.repository.TaskRepository;
import org.example.taskflow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public Task createTask(Long projectId, TaskRequest request){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User currentUser = getCurrentUser();

        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);

        if (!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be in the past");
        }
        task.setDueDate(request.getDueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setProject(project);
        task.setCreatedBy(currentUser);

        User assignedUser = null;
        if (request.getAssignedUsername() != null && !request.getAssignedUsername().isBlank()){
            if (!isOwner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can assign tasks");
            }
            assignedUser = userRepository.findByUsername(request.getAssignedUsername()).orElse(null);
            if (assignedUser == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found");
            }
            boolean assignedIsOwner = project.getOwner().getId().equals(assignedUser.getId());
            boolean assignedIsMember = projectMemberRepository.existsByProjectAndUser(project, assignedUser);

            if(!assignedIsOwner && !assignedIsMember){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user is not a member of this project");
            }
            task.setAssignedUser(assignedUser);
        }
        return taskRepository.save(task);
    }

    public List<Task> getProjectTasks(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        User currentUser = getCurrentUser();
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);

        if(!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }
        return taskRepository.findByProject(project);
    }

    public Task getById(Long projectId, Long taskId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        User currentUser = getCurrentUser();
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);
        if (!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this project");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!task.getProject().getId().equals(projectId)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not belong to this project");
        }
        return task;
    }

    public Task updateTask(Long taskId, TaskRequest request){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User currentUser = getCurrentUser();

        boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        boolean isAssigned = task.getAssignedUser() != null && task.getAssignedUser().getId().equals(currentUser.getId());
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUser.getId());
        if(!isOwner && !isAssigned && !isCreator){
            throw  new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update task");
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null){
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null){
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null){
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be in the past");
        }
        if (request.getDueDate() != null){
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssignedUsername() != null && !request.getAssignedUsername().isBlank()) {
            if (!isOwner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can reassign tasks");
            }
            User assignedUser = userRepository.findByUsername(request.getAssignedUsername()).orElse(null);
            if (assignedUser == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found");
            }

            boolean assignedIsOwner = task.getProject().getOwner().getId().equals(assignedUser.getId());
            boolean assignedIsMember = projectMemberRepository.existsByProjectAndUser(task.getProject(), assignedUser);
            if (!assignedIsOwner && !assignedIsMember) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user is not a member of this project");
            }
            task.setAssignedUser(assignedUser);
        }
        return taskRepository.save(task);
    }

    public void deleteTask(Long id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User currentUser = getCurrentUser();

        boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        if (!isOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can delete task");
        }
        taskRepository.delete(task);
    }
}

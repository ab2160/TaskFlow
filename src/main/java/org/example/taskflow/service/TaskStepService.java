package org.example.taskflow.service;

import org.example.taskflow.dto.TaskRequest;
import org.example.taskflow.dto.TaskStepRequest;
import org.example.taskflow.entity.Task;
import org.example.taskflow.entity.TaskStep;
import org.example.taskflow.entity.User;
import org.example.taskflow.repository.ProjectMemberRepository;
import org.example.taskflow.repository.TaskRepository;
import org.example.taskflow.repository.TaskStepRepository;
import org.example.taskflow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskStepService {
    private final TaskStepRepository taskStepRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public TaskStepService(TaskStepRepository taskStepRepository, TaskRepository taskRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.taskStepRepository = taskStepRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    private Task getAuthorizedTask(Long taskId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User currentUser = getCurrentUser();

        boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        boolean isAssigned = task.getAssignedUser() != null && task.getAssignedUser().getId().equals(currentUser.getId());
        if (!isOwner && !isAssigned){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or assigned user can manage steps");
        }
        return task;
    }

    public TaskStep addStep(Long taskId, TaskStepRequest request){
        Task task = getAuthorizedTask(taskId);
        TaskStep step = new TaskStep();

        step.setDescription(request.getDescription());
        step.setCompleted(false);
        step.setCreatedAt(LocalDateTime.now());
        step.setTask(task);

        return taskStepRepository.save(step);
    }

    public List<TaskStep> getTaskSteps(Long taskId){
        Task task = getAuthorizedTask(taskId);
        return taskStepRepository.findByTask(task);
    }

    public TaskStep updateStep(Long stepId, TaskStepRequest request){
        TaskStep step = taskStepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found"));

        getAuthorizedTask(step.getTask().getId());
        if (request.getDescription() != null && !request.getDescription().isBlank()){
            step.setDescription(request.getDescription());
        }

        return taskStepRepository.save(step);
    }

    public TaskStep markStepCompleted(Long stepId){
        TaskStep step = taskStepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found"));
        getAuthorizedTask(step.getTask().getId());

        step.setCompleted(true);
        return taskStepRepository.save(step);
    }

    public TaskStep uncompleteStep(Long stepId){
        TaskStep step = taskStepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found"));
        getAuthorizedTask(step.getTask().getId());

        step.setCompleted(false);
        return taskStepRepository.save(step);
    }

    public void deleteStep(Long stepId){
        TaskStep step = taskStepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found"));
        getAuthorizedTask(step.getTask().getId());

        taskStepRepository.delete(step);
    }

    public double getTaskProgress(Long taskId){
        Task task = getAuthorizedTask(taskId);
        List<TaskStep> steps = taskStepRepository.findByTask(task);

        if (steps.isEmpty()){
            return 0;
        }
        long completed = steps.stream().filter(TaskStep::isCompleted).count();
        return (completed * 100.0) / steps.size();
    }
}

package org.example.taskflow.repository;

import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.Task;
import org.example.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignedUser(User user);
    List<Task> findByProjectAndAssignedUser(Project project, User user);
}

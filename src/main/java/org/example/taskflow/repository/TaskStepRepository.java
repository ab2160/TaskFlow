package org.example.taskflow.repository;

import org.example.taskflow.entity.Task;
import org.example.taskflow.entity.TaskStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {
    List<TaskStep> findByTask(Task task);
}

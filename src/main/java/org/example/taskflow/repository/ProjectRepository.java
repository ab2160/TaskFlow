package org.example.taskflow.repository;

import org.example.taskflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectRepository extends JpaRepository<Project, Long>{
}

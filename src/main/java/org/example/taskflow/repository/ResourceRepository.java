package org.example.taskflow.repository;

import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByProject(Project project);
}

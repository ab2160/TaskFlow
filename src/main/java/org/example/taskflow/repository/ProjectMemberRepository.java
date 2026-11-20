package org.example.taskflow.repository;

import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.ProjectMember;
import org.example.taskflow.entity.ProjectMemberId;
import org.example.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProject(Project project);
    List<ProjectMember> findByUser(User user);

    Optional<ProjectMember> findByProjectAndUser(
            Project project,
            User user);
    boolean existsByProjectAndUser(
            Project project,
            User user
    );
}

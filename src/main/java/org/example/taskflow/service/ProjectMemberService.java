package org.example.taskflow.service;

import org.example.taskflow.dto.AddProjectMemberRequest;
import org.example.taskflow.dto.ProjectMemberResponse;
import org.example.taskflow.entity.Project;
import org.example.taskflow.entity.ProjectMember;
import org.example.taskflow.entity.ProjectMemberId;
import org.example.taskflow.entity.User;
import org.example.taskflow.repository.ProjectMemberRepository;
import org.example.taskflow.repository.ProjectRepository;
import org.example.taskflow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectMemberService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }
    public ProjectMember addMember(Long projectId, AddProjectMemberRequest request){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);
        if (!project.getOwner().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can add members");
        }
        User member = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (projectMemberRepository.existsByProjectAndUser(project, member)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already a member");
        }
        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(new ProjectMemberId(project.getId(), member.getId()));
        projectMember.setProject(project);
        projectMember.setUser(member);
        projectMember.setRole("MEMBER");

        return projectMemberRepository.save(projectMember);
    }

    public List<ProjectMemberResponse> getMembers (Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);
        
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);

        if(!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this project");
        }

        return projectMemberRepository.findByProject(project)
                .stream()
                .map(member ->
                        new ProjectMemberResponse(
                                member.getUser().getId(),
                                member.getUser().getName(),
                                member.getUser().getEmail(),
                                member.getRole()
                        )).toList();
    }

    public void removeMember(Long projectId, Long userId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email);
        if (!project.getOwner().getId().equals(currentUser.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can remove members");
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProjectMember projectMember = projectMemberRepository.findByProjectAndUser(project, member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        projectMemberRepository.delete(projectMember);
    }
}

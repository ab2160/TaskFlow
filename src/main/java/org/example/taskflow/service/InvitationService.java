package org.example.taskflow.service;

import org.example.taskflow.dto.InvitationRequest;
import org.example.taskflow.dto.InvitationResponse;
import org.example.taskflow.entity.*;
import org.example.taskflow.enums.InvitationStatus;
import org.example.taskflow.repository.InvitationRepository;
import org.example.taskflow.repository.ProjectMemberRepository;
import org.example.taskflow.repository.ProjectRepository;
import org.example.taskflow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public InvitationService(InvitationRepository invitationRepository, ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public Invitation sendInvitation(Long projectId, InvitationRequest request){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User currentUser = getCurrentUser();
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can send invitations");
        }
        User invitedUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        boolean alreadyMember = projectMemberRepository.existsByProjectAndUser(project, invitedUser);
        if (alreadyMember){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a member");
        }
        boolean pendingInvitationExists = invitationRepository.findByProjectAndInviteeEmailAndStatus(
                project, invitedUser.getEmail(), InvitationStatus.PENDING).isPresent();
        if (pendingInvitationExists){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation already sent");
        }

        Invitation invitation = new Invitation();

        invitation.setProject(project);
        invitation.setInviteeEmail(invitedUser.getEmail());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setCreatedAt(LocalDateTime.now());

        return invitationRepository.save(invitation);
    }

    public List<Invitation> getMyInvitations(){
        User currentUser = getCurrentUser();
        return invitationRepository.findByInviteeEmail(currentUser.getEmail());
    }

    public InvitationResponse acceptInvitation(String token){
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        User currentUser = getCurrentUser();
        if (!currentUser.getEmail().equals(invitation.getInviteeEmail())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation is not yours");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation already processed");
        }
        Project project = invitation.getProject();
        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(new ProjectMemberId(project.getId(), currentUser.getId()));
        projectMember.setProject(project);
        projectMember.setUser(currentUser);
        projectMember.setRole("MEMBER");
        projectMemberRepository.save(projectMember);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        return new InvitationResponse("Invitation accepted");
    }

    public InvitationResponse declineInvitation(String token){
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        User currentUser = getCurrentUser();
        if (!currentUser.getEmail().equals(invitation.getInviteeEmail())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This invitation is not yours");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation already processed");
        }
        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);

        return new InvitationResponse("Invitation declined");
    }
}

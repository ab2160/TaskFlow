package org.example.taskflow.repository;

import org.example.taskflow.entity.Invitation;
import org.example.taskflow.entity.Project;
import org.example.taskflow.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByInviteeEmail(String inviteeEmail);
    Optional<Invitation> findByProjectAndInviteeEmailAndStatus(Project project, String inviteeEmail, InvitationStatus status);

}

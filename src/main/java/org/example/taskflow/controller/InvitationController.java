package org.example.taskflow.controller;

import org.example.taskflow.dto.InvitationRequest;
import org.example.taskflow.dto.InvitationResponse;
import org.example.taskflow.entity.Invitation;
import org.example.taskflow.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/projects/{projectId}/invitations")
    public ResponseEntity<Invitation> sendInvitation(@PathVariable Long projectId, @RequestBody InvitationRequest request){
        return ResponseEntity.ok(invitationService.sendInvitation(projectId, request));
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<Invitation>> getMyInvitations(){
        return ResponseEntity.ok(invitationService.getMyInvitations());
    }

    @PostMapping("/invitations/accept/{token}")
    public ResponseEntity<InvitationResponse> acceptInvitation(@PathVariable String token){
        return ResponseEntity.ok(invitationService.acceptInvitation(token));
    }

    @PostMapping("/invitations/decline/{token}")
    public ResponseEntity<InvitationResponse> declineInvitation(@PathVariable String token){
        return ResponseEntity.ok(invitationService.declineInvitation(token));
    }

}

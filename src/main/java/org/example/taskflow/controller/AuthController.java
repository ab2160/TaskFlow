package org.example.taskflow.controller;

import org.example.taskflow.dto.RegisterRequest;
import org.example.taskflow.dto.LoginRequest;
import org.example.taskflow.entity.User;
import org.example.taskflow.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        Optional<User> user = authService.register(request);
        if(user.isPresent()){
            return ResponseEntity.status(201).body("Registration successful");
        }
        return ResponseEntity.badRequest().body("Email already exists");
    }

   @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request){
        Optional<User> user = authService.login(request);
        if(user.isPresent()){
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.badRequest().body("Invalid email or password");
   }
}

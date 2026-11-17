package org.example.taskflow.service;

import org.example.taskflow.entity.User;
import org.example.taskflow.repository.UserRepository;
import org.example.taskflow.dto.RegisterRequest;
import org.example.taskflow.dto.LoginRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;


    public AuthService(UserRepository repo, BCryptPasswordEncoder passwordEncoder) {

        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request){
        User existing = repo.findByEmail(request.getEmail());
        if (existing != null){
            return "Email already exists";
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        repo.save(user);

        return "Registration successful";
    }

    public String login(LoginRequest request){
        User user = repo.findByEmail(request.getEmail());
        if (user == null){
            return "User not found";
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            return "Invalid password";
        }
        return "Login successful";
    }
}

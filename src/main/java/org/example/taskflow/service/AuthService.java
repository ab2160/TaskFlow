package org.example.taskflow.service;

import org.example.taskflow.entity.User;
import org.example.taskflow.repository.UserRepository;
import org.example.taskflow.dto.RegisterRequest;
import org.example.taskflow.dto.LoginRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository repo;

    public AuthService(UserRepository repo) {
        this.repo = repo;
    }

    public String register(RegisterRequest request){
        User existing = repo.findByEmail(request.getEmail());
        if (existing != null){
            return "Email already exists";
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        repo.save(user);

        return "Registration successful";
    }

    public String login(LoginRequest request){
        User user = repo.findByEmail(request.getEmail());
        if (user == null){
            return "User not found";
        }
        if(!user.getPassword().equals(request.getPassword())){
            return "Invalid password";
        }
        return "Login successful";
    }
}

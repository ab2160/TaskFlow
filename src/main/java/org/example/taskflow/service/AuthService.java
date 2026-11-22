package org.example.taskflow.service;

import org.example.taskflow.entity.User;
import org.example.taskflow.repository.UserRepository;
import org.example.taskflow.dto.RegisterRequest;
import org.example.taskflow.dto.LoginRequest;
import org.example.taskflow.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthService(UserRepository repo, BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Optional<User> register(RegisterRequest request){
        User existing = repo.findByEmail(request.getEmail());
        if (existing != null){
            return Optional.empty();
        }
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return Optional.of(repo.save(user));
    }

    public Optional<String> login(LoginRequest request){
        User user = repo.findByEmail(request.getEmail());
        if(user == null){
            return Optional.empty();
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            return Optional.empty();
        }
        return Optional.of(jwtService.generateToken(user.getEmail()));
    }
}

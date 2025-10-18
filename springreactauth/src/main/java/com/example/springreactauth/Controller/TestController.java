package com.example.springreactauth.Controller;

import com.example.springreactauth.Entity.User;
import com.example.springreactauth.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // Optional, but good practice
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()") // Ensure the user is logged in
    public List<User> getAllUsers() {
        // NOTE: In a real app, you should NOT expose raw passwords/hashes
        return userRepository.findAll();
    }
}
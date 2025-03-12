package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;

import java.util.List;

public interface UserService {
    
    User registerUser(RegisterRequest registerRequest);
    
    User findByUsername(String username);
    
    User findById(Long id);
    
    List<UserDTO> findAllUsers();
    
    UserDTO updateUser(Long id, RegisterRequest registerRequest);
    
    void deleteUser(Long id);
    
    boolean existsByUsername(String username);
} 
package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.JwtResponse;
import com.example.demo.entity.User;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("尝试登录用户: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            logger.info("用户 {} 登录成功，权限: {}", userDetails.getUsername(), userDetails.getAuthorities());
            
            User user = userService.findByUsername(userDetails.getUsername());
            logger.info("用户角色: {}", user.getRole());
            
            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getName(),
                    user.getRole()
            );
            
            logger.info("生成的JWT响应: {}", jwtResponse);
            
            return ResponseEntity.ok(new ApiResponse(true, "登录成功", jwtResponse));
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "登录失败: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("尝试注册用户: {}", registerRequest.getUsername());
            
            if (userService.existsByUsername(registerRequest.getUsername())) {
                logger.warn("用户名已存在: {}", registerRequest.getUsername());
                return ResponseEntity.badRequest().body(ApiResponse.error("用户名已存在"));
            }
            
            User user = userService.registerUser(registerRequest);
            logger.info("用户注册成功: {}, 角色: {}", user.getUsername(), user.getRole());
            
            return ResponseEntity.ok(ApiResponse.success("用户注册成功", user.getUsername()));
        } catch (Exception e) {
            logger.error("注册失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "注册失败: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkAuthStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("检查认证状态: {}", authentication);
            
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                logger.info("用户已认证: {}, 权限: {}", userDetails.getUsername(), userDetails.getAuthorities());
                
                return ResponseEntity.ok(new ApiResponse(true, "用户已认证", userDetails.getUsername()));
            } else {
                logger.info("用户未认证");
                return ResponseEntity.ok(new ApiResponse(false, "用户未认证", null));
            }
        } catch (Exception e) {
            logger.error("检查认证状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "检查认证状态失败: " + e.getMessage(), null));
        }
    }
} 
package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.BorrowDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.Borrow;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getStats() {
        try {
            // 记录当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("当前用户: {}, 权限: {}", authentication.getName(), authentication.getAuthorities());
            
            long totalBooks = bookRepository.count();
            long totalUsers = userRepository.count();
            long totalBorrows = borrowRepository.count();
            
            // 使用枚举值的字符串表示
            long pendingBorrows = borrowRepository.countByStatus(Borrow.Status.PENDING.name());
            
            logger.info("统计数据: 图书总数={}, 用户总数={}, 借阅总数={}, 待处理借阅={}", 
                    totalBooks, totalUsers, totalBorrows, pendingBorrows);

            Map<String, Long> stats = new HashMap<>();
            stats.put("totalBooks", totalBooks);
            stats.put("totalUsers", totalUsers);
            stats.put("totalBorrows", totalBorrows);
            stats.put("pendingBorrows", pendingBorrows);

            return ResponseEntity.ok(new ApiResponse(true, "操作成功", stats));
        } catch (Exception e) {
            logger.error("获取统计数据失败", e);
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "获取统计数据失败: " + e.getMessage(), null));
        }
    }

    @GetMapping("/borrows/recent")
    public ResponseEntity<ApiResponse> getRecentBorrows() {
        try {
            List<Borrow> recentBorrows = borrowRepository.findTop10ByOrderByCreateTimeDesc();
            
            List<BorrowDTO> borrowDTOs = recentBorrows.stream().map(borrow -> {
                BorrowDTO dto = new BorrowDTO();
                dto.setId(borrow.getId());
                dto.setStatus(borrow.getStatus().name());
                dto.setBorrowDate(borrow.getBorrowDate());
                dto.setDueDate(borrow.getDueDate());
                dto.setReturnDate(borrow.getReturnDate());
                
                // 获取用户信息
                User user = borrow.getUser();
                if (user != null) {
                    dto.setUserId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setUserRealName(user.getName());
                }
                
                // 获取图书信息
                Book book = borrow.getBook();
                if (book != null) {
                    dto.setBookId(book.getId());
                    dto.setBookTitle(book.getTitle());
                    dto.setBookIsbn(book.getIsbn());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponse(true, "操作成功", borrowDTOs));
        } catch (Exception e) {
            logger.error("获取最近借阅失败", e);
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "获取最近借阅失败: " + e.getMessage(), null));
        }
    }
} 
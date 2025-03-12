package com.example.demo.controller;

import com.example.demo.dto.BorrowDTO;
import com.example.demo.dto.request.BorrowRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Borrow;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BorrowController {
    
    @Autowired
    private BorrowService borrowService;
    
    @PostMapping
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<ApiResponse<BorrowDTO>> borrowBook(@Valid @RequestBody BorrowRequest borrowRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        BorrowDTO borrow = borrowService.borrowBook(userDetails.getId(), borrowRequest);
        return ResponseEntity.ok(ApiResponse.success("借阅申请提交成功，等待管理员审核", borrow));
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowDTO>> approveBorrow(@PathVariable Long id) {
        BorrowDTO borrow = borrowService.approveBorrow(id);
        return ResponseEntity.ok(ApiResponse.success("借阅申请已批准", borrow));
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowDTO>> rejectBorrow(@PathVariable Long id) {
        BorrowDTO borrow = borrowService.rejectBorrow(id);
        return ResponseEntity.ok(ApiResponse.success("借阅申请已拒绝", borrow));
    }
    
    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BorrowDTO>> returnBook(@PathVariable Long id) {
        BorrowDTO borrow = borrowService.returnBook(id);
        return ResponseEntity.ok(ApiResponse.success("图书已归还", borrow));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @borrowSecurity.isOwner(#id)")
    public ResponseEntity<ApiResponse<BorrowDTO>> getBorrowById(@PathVariable Long id) {
        BorrowDTO borrow = borrowService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(borrow));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BorrowDTO>>> getAllBorrows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowDTO> borrows = borrowService.findAllBorrows(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(borrows));
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<ApiResponse<Page<BorrowDTO>>> getBorrowsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowDTO> borrows = borrowService.findBorrowsByUser(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(borrows));
    }
    
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BorrowDTO>>> getBorrowsByBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowDTO> borrows = borrowService.findBorrowsByBook(bookId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(borrows));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BorrowDTO>>> getBorrowsByStatus(
            @PathVariable Borrow.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowDTO> borrows = borrowService.findBorrowsByStatus(status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(borrows));
    }
    
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BorrowDTO>>> getOverdueBooks() {
        List<BorrowDTO> borrows = borrowService.findOverdueBooks();
        return ResponseEntity.ok(ApiResponse.success(borrows));
    }
    
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<BorrowDTO>>> getCurrentBorrows() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<BorrowDTO> borrows = borrowService.findCurrentBorrowsByUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(borrows));
    }
} 
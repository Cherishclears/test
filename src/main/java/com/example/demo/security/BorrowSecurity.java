package com.example.demo.security;

import com.example.demo.dto.BorrowDTO;
import com.example.demo.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("borrowSecurity")
public class BorrowSecurity {
    
    @Autowired
    private BorrowService borrowService;
    
    public boolean isOwner(Long borrowId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        BorrowDTO borrow = borrowService.findById(borrowId);
        
        return borrow.getUserId().equals(userDetails.getId());
    }
} 
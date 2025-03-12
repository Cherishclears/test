package com.example.demo.service;

import com.example.demo.dto.BorrowDTO;
import com.example.demo.dto.request.BorrowRequest;
import com.example.demo.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowService {
    
    BorrowDTO borrowBook(Long userId, BorrowRequest borrowRequest);
    
    BorrowDTO approveBorrow(Long id);
    
    BorrowDTO rejectBorrow(Long id);
    
    BorrowDTO returnBook(Long id);
    
    BorrowDTO findById(Long id);
    
    Page<BorrowDTO> findAllBorrows(Pageable pageable);
    
    Page<BorrowDTO> findBorrowsByUser(Long userId, Pageable pageable);
    
    Page<BorrowDTO> findBorrowsByBook(Long bookId, Pageable pageable);
    
    Page<BorrowDTO> findBorrowsByStatus(Borrow.Status status, Pageable pageable);
    
    List<BorrowDTO> findOverdueBooks();
    
    List<BorrowDTO> findCurrentBorrowsByUser(Long userId);
} 
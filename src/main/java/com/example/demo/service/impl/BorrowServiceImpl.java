package com.example.demo.service.impl;

import com.example.demo.dto.BorrowDTO;
import com.example.demo.dto.request.BorrowRequest;
import com.example.demo.entity.Book;
import com.example.demo.entity.Borrow;
import com.example.demo.entity.User;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.service.BookService;
import com.example.demo.service.BorrowService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowServiceImpl implements BorrowService {
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Override
    @Transactional
    public BorrowDTO borrowBook(Long userId, BorrowRequest borrowRequest) {
        User user = userService.findById(userId);
        Book book = bookService.getBookEntity(borrowRequest.getBookId());
        
        // 检查图书是否可借
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("图书已全部借出");
        }
        
        // 创建借阅记录
        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        
        // 设置借阅日期和归还日期
        LocalDate today = LocalDate.now();
        borrow.setBorrowDate(borrowRequest.getBorrowDate() != null ? borrowRequest.getBorrowDate() : today);
        
        // 默认借阅期限为30天
        borrow.setDueDate(borrowRequest.getDueDate() != null ? borrowRequest.getDueDate() : today.plusDays(30));
        
        // 设置状态为待审核
        borrow.setStatus(Borrow.Status.PENDING);
        
        return BorrowDTO.fromEntity(borrowRepository.save(borrow));
    }
    
    @Override
    @Transactional
    public BorrowDTO approveBorrow(Long id) {
        Borrow borrow = findBorrowById(id);
        
        // 检查状态是否为待审核
        if (borrow.getStatus() != Borrow.Status.PENDING) {
            throw new RuntimeException("只能审核待处理的借阅请求");
        }
        
        // 检查图书是否可借
        Book book = borrow.getBook();
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("图书已全部借出");
        }
        
        // 更新借阅状态
        borrow.setStatus(Borrow.Status.APPROVED);
        
        // 更新图书可用数量
        bookService.updateBookAvailability(book.getId(), -1);
        
        return BorrowDTO.fromEntity(borrowRepository.save(borrow));
    }
    
    @Override
    @Transactional
    public BorrowDTO rejectBorrow(Long id) {
        Borrow borrow = findBorrowById(id);
        
        // 检查状态是否为待审核
        if (borrow.getStatus() != Borrow.Status.PENDING) {
            throw new RuntimeException("只能拒绝待处理的借阅请求");
        }
        
        // 更新借阅状态
        borrow.setStatus(Borrow.Status.REJECTED);
        
        return BorrowDTO.fromEntity(borrowRepository.save(borrow));
    }
    
    @Override
    @Transactional
    public BorrowDTO returnBook(Long id) {
        Borrow borrow = findBorrowById(id);
        
        // 检查状态是否为已批准
        if (borrow.getStatus() != Borrow.Status.APPROVED && borrow.getStatus() != Borrow.Status.OVERDUE) {
            throw new RuntimeException("只能归还已批准或逾期的借阅");
        }
        
        // 更新借阅状态和归还日期
        borrow.setStatus(Borrow.Status.RETURNED);
        borrow.setReturnDate(LocalDate.now());
        
        // 更新图书可用数量
        bookService.updateBookAvailability(borrow.getBook().getId(), 1);
        
        return BorrowDTO.fromEntity(borrowRepository.save(borrow));
    }
    
    @Override
    public BorrowDTO findById(Long id) {
        return BorrowDTO.fromEntity(findBorrowById(id));
    }
    
    private Borrow findBorrowById(Long id) {
        return borrowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到借阅记录ID: " + id));
    }
    
    @Override
    public Page<BorrowDTO> findAllBorrows(Pageable pageable) {
        return borrowRepository.findAll(pageable).map(BorrowDTO::fromEntity);
    }
    
    @Override
    public Page<BorrowDTO> findBorrowsByUser(Long userId, Pageable pageable) {
        User user = userService.findById(userId);
        return borrowRepository.findByUser(user, pageable).map(BorrowDTO::fromEntity);
    }
    
    @Override
    public Page<BorrowDTO> findBorrowsByBook(Long bookId, Pageable pageable) {
        Book book = bookService.getBookEntity(bookId);
        return borrowRepository.findByBook(book, pageable).map(BorrowDTO::fromEntity);
    }
    
    @Override
    public Page<BorrowDTO> findBorrowsByStatus(Borrow.Status status, Pageable pageable) {
        return borrowRepository.findByStatus(status, pageable).map(BorrowDTO::fromEntity);
    }
    
    @Override
    public List<BorrowDTO> findOverdueBooks() {
        LocalDate today = LocalDate.now();
        return borrowRepository.findOverdueBooks(today).stream()
                .map(BorrowDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BorrowDTO> findCurrentBorrowsByUser(Long userId) {
        User user = userService.findById(userId);
        return borrowRepository.findByUserAndStatus(user, Borrow.Status.APPROVED).stream()
                .map(BorrowDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 
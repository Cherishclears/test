package com.example.demo.repository;

import com.example.demo.entity.Book;
import com.example.demo.entity.Borrow;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    
    List<Borrow> findByUser(User user);
    
    Page<Borrow> findByUser(User user, Pageable pageable);
    
    List<Borrow> findByBook(Book book);
    
    Page<Borrow> findByBook(Book book, Pageable pageable);
    
    List<Borrow> findByStatus(Borrow.Status status);
    
    Page<Borrow> findByStatus(Borrow.Status status, Pageable pageable);
    
    @Query("SELECT b FROM Borrow b WHERE b.status = 'APPROVED' AND b.dueDate < :today AND b.returnDate IS NULL")
    List<Borrow> findOverdueBooks(LocalDate today);
    
    List<Borrow> findByUserAndStatus(User user, Borrow.Status status);
    
    List<Borrow> findByBookAndStatus(Book book, Borrow.Status status);
    
    List<Borrow> findByUserId(Long userId);
    
    List<Borrow> findByBookId(Long bookId);
    
    List<Borrow> findByStatus(String status);
    
    long countByStatus(String status);
    
    List<Borrow> findByStatusAndDueDateBeforeAndReturnDateIsNull(String status, Date currentDate);
    
    List<Borrow> findTop10ByOrderByCreateTimeDesc();
} 
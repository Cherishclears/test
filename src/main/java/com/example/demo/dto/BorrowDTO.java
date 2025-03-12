package com.example.demo.dto;

import com.example.demo.entity.Borrow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private String userRealName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private LocalDateTime createTime;
    
    public static BorrowDTO fromEntity(Borrow borrow) {
        BorrowDTO dto = new BorrowDTO();
        dto.setId(borrow.getId());
        dto.setUserId(borrow.getUser().getId());
        dto.setUsername(borrow.getUser().getUsername());
        dto.setUserRealName(borrow.getUser().getName());
        dto.setBookId(borrow.getBook().getId());
        dto.setBookTitle(borrow.getBook().getTitle());
        dto.setBookIsbn(borrow.getBook().getIsbn());
        dto.setBorrowDate(borrow.getBorrowDate());
        dto.setDueDate(borrow.getDueDate());
        dto.setReturnDate(borrow.getReturnDate());
        dto.setStatus(borrow.getStatus().name());
        dto.setCreateTime(borrow.getCreateTime());
        return dto;
    }
} 
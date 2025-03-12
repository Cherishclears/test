package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BorrowRequest {
    
    @NotNull(message = "图书ID不能为空")
    private Long bookId;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate borrowDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
} 
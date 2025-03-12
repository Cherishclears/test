package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BookRequest {
    
    @NotBlank(message = "ISBN不能为空")
    private String isbn;
    
    @NotBlank(message = "书名不能为空")
    private String title;
    
    @NotBlank(message = "作者不能为空")
    private String author;
    
    private String publisher;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    
    @NotBlank(message = "分类不能为空")
    private String category;
    
    private String description;
    
    private String cover;
    
    private String location;
    
    @NotNull(message = "总藏书数量不能为空")
    @Min(value = 1, message = "总藏书数量必须大于0")
    private Integer totalCopies;
} 
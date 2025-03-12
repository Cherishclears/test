package com.example.demo.controller;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.request.BookRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.BookService;
import com.example.demo.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDTO>> addBook(@Valid @RequestBody BookRequest bookRequest) {
        BookDTO book = bookService.addBook(bookRequest);
        return ResponseEntity.ok(ApiResponse.success("图书添加成功", book));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        BookDTO book = bookService.updateBook(id, bookRequest);
        return ResponseEntity.ok(ApiResponse.success("图书更新成功", book));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("图书删除成功", null));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }
    
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookByIsbn(@PathVariable String isbn) {
        BookDTO book = bookService.findByIsbn(isbn);
        return ResponseEntity.ok(ApiResponse.success(book));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<BookDTO> books = bookService.findAllBooks(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.searchBooks(keyword, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.findByCategory(category, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.findByAuthor(author, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<BookDTO>>> getAvailableBooks() {
        List<BookDTO> books = bookService.findAvailableBooks();
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    // 公开API，不需要认证
    @GetMapping("/public/search")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> publicSearchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.searchBooks(keyword, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(books));
    }
    
    @PostMapping("/upload-cover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadBookCover(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileStorageService.storeFile(file);
        return ResponseEntity.ok(ApiResponse.success("图书封面上传成功", fileUrl));
    }
} 
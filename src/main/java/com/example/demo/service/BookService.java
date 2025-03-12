package com.example.demo.service;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.request.BookRequest;
import com.example.demo.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    
    BookDTO addBook(BookRequest bookRequest);
    
    BookDTO updateBook(Long id, BookRequest bookRequest);
    
    void deleteBook(Long id);
    
    BookDTO findById(Long id);
    
    Book getBookEntity(Long id);
    
    BookDTO findByIsbn(String isbn);
    
    Page<BookDTO> findAllBooks(Pageable pageable);
    
    Page<BookDTO> searchBooks(String keyword, Pageable pageable);
    
    Page<BookDTO> findByCategory(String category, Pageable pageable);
    
    Page<BookDTO> findByAuthor(String author, Pageable pageable);
    
    List<BookDTO> findAvailableBooks();
    
    boolean existsByIsbn(String isbn);
    
    void updateBookStatus(Long id, Book.Status status);
    
    void updateBookAvailability(Long id, int change);
} 
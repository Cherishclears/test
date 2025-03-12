package com.example.demo.service.impl;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.request.BookRequest;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import com.example.demo.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Override
    @Transactional
    public BookDTO addBook(BookRequest bookRequest) {
        // 检查ISBN是否已存在
        if (bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new RuntimeException("ISBN已存在");
        }
        
        Book book = new Book();
        book.setIsbn(bookRequest.getIsbn());
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setPublisher(bookRequest.getPublisher());
        book.setPublishDate(bookRequest.getPublishDate());
        book.setCategory(bookRequest.getCategory());
        book.setDescription(bookRequest.getDescription());
        book.setCover(bookRequest.getCover());
        book.setLocation(bookRequest.getLocation());
        book.setTotalCopies(bookRequest.getTotalCopies());
        book.setAvailableCopies(bookRequest.getTotalCopies()); // 初始可用数量等于总数量
        book.setStatus(Book.Status.AVAILABLE);
        
        return BookDTO.fromEntity(bookRepository.save(book));
    }
    
    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookRequest bookRequest) {
        Book book = getBookEntity(id);
        
        // 如果更改了ISBN，检查新ISBN是否已存在
        if (!book.getIsbn().equals(bookRequest.getIsbn()) &&
                bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new RuntimeException("ISBN已存在");
        }
        
        book.setIsbn(bookRequest.getIsbn());
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setPublisher(bookRequest.getPublisher());
        book.setPublishDate(bookRequest.getPublishDate());
        book.setCategory(bookRequest.getCategory());
        book.setDescription(bookRequest.getDescription());
        book.setCover(bookRequest.getCover());
        book.setLocation(bookRequest.getLocation());
        
        // 更新总数量和可用数量
        int diff = bookRequest.getTotalCopies() - book.getTotalCopies();
        book.setTotalCopies(bookRequest.getTotalCopies());
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + diff));
        
        // 更新状态
        if (book.getAvailableCopies() > 0) {
            book.setStatus(Book.Status.AVAILABLE);
        } else {
            book.setStatus(Book.Status.BORROWED);
        }
        
        return BookDTO.fromEntity(bookRepository.save(book));
    }
    
    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookEntity(id);
        bookRepository.delete(book);
    }
    
    @Override
    public BookDTO findById(Long id) {
        return BookDTO.fromEntity(getBookEntity(id));
    }
    
    @Override
    public Book getBookEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到图书ID: " + id));
    }
    
    @Override
    public BookDTO findByIsbn(String isbn) {
        return BookDTO.fromEntity(bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("未找到ISBN: " + isbn)));
    }
    
    @Override
    public Page<BookDTO> findAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookDTO::fromEntity);
    }
    
    @Override
    public Page<BookDTO> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.searchBooks(keyword, pageable).map(BookDTO::fromEntity);
    }
    
    @Override
    public Page<BookDTO> findByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable).map(BookDTO::fromEntity);
    }
    
    @Override
    public Page<BookDTO> findByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable).map(BookDTO::fromEntity);
    }
    
    @Override
    public List<BookDTO> findAvailableBooks() {
        return bookRepository.findByStatus(Book.Status.AVAILABLE).stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }
    
    @Override
    @Transactional
    public void updateBookStatus(Long id, Book.Status status) {
        Book book = getBookEntity(id);
        book.setStatus(status);
        bookRepository.save(book);
    }
    
    @Override
    @Transactional
    public void updateBookAvailability(Long id, int change) {
        Book book = getBookEntity(id);
        int newAvailable = book.getAvailableCopies() + change;
        
        if (newAvailable < 0) {
            throw new RuntimeException("可用数量不能小于0");
        }
        
        if (newAvailable > book.getTotalCopies()) {
            throw new RuntimeException("可用数量不能大于总数量");
        }
        
        book.setAvailableCopies(newAvailable);
        
        // 更新状态
        if (newAvailable > 0) {
            book.setStatus(Book.Status.AVAILABLE);
        } else {
            book.setStatus(Book.Status.BORROWED);
        }
        
        bookRepository.save(book);
    }
} 
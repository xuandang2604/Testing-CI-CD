package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.dto.api.ProductApiRequest;
import com.example.xuandangwebbanhang.dto.api.ProductApiResponse;
import com.example.xuandangwebbanhang.model.Book;
import com.example.xuandangwebbanhang.model.BookCategory;
import com.example.xuandangwebbanhang.repository.BookCategoryRepository;
import com.example.xuandangwebbanhang.repository.BookRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {
    private final BookRepository bookRepository;
    private final BookCategoryRepository categoryRepository;

    @GetMapping
    public List<ProductApiResponse> getAllProducts() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductApiResponse> getProductById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(book -> ResponseEntity.ok(toResponse(book)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductApiResponse> createProduct(@Valid @RequestBody ProductApiRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        Book saved = bookRepository.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductApiResponse> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody ProductApiRequest request) {
        return bookRepository.findById(id)
                .map(existing -> {
                    applyRequest(existing, request);
                    Book saved = bookRepository.save(existing);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(Book book, ProductApiRequest request) {
        book.setName(request.getName());
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());

        if (request.getCategoryId() == null) {
            book.setCategory(null);
            return;
        }

        BookCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
        book.setCategory(category);
    }

    private ProductApiResponse toResponse(Book book) {
        ProductApiResponse response = new ProductApiResponse();
        response.setId(book.getId());
        response.setName(book.getName());
        response.setPrice(book.getPrice());
        response.setDescription(book.getDescription());
        response.setCategoryId(book.getCategory() != null ? book.getCategory().getId() : null);
        response.setCategoryName(book.getCategory() != null ? book.getCategory().getName() : null);
        return response;
    }
}

package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.dto.api.CategoryApiRequest;
import com.example.xuandangwebbanhang.dto.api.CategoryApiResponse;
import com.example.xuandangwebbanhang.model.BookCategory;
import com.example.xuandangwebbanhang.repository.BookCategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApiController {
    private final BookCategoryRepository categoryRepository;

    @GetMapping
    public List<CategoryApiResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryApiResponse> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(category -> ResponseEntity.ok(toResponse(category)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryApiResponse> createCategory(@Valid @RequestBody CategoryApiRequest request) {
        BookCategory category = new BookCategory();
        applyRequest(category, request);
        BookCategory saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryApiResponse> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody CategoryApiRequest request) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    applyRequest(existing, request);
                    BookCategory saved = categoryRepository.save(existing);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(BookCategory category, CategoryApiRequest request) {
        category.setName(request.getName());
        String description = request.getDescription();
        if (!StringUtils.hasText(description)) {
            description = request.getIcon();
        }
        category.setDescription(description);
    }

    private CategoryApiResponse toResponse(BookCategory category) {
        CategoryApiResponse response = new CategoryApiResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setIcon(category.getDescription());
        return response;
    }
}

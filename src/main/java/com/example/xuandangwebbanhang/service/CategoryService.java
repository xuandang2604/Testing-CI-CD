package com.example.xuandangwebbanhang.service;

import com.example.xuandangwebbanhang.model.Category;
import com.example.xuandangwebbanhang.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing categories.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /** Tất cả danh mục */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /** Chỉ danh mục gốc (parent == null) */
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    /** Danh mục con theo parent id */
    public List<Category> getChildrenByParentId(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public void updateCategory(@NotNull Category category) {
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new IllegalStateException("Category with ID " +
                        category.getId() + " does not exist."));
        existingCategory.setName(category.getName());
        existingCategory.setIcon(category.getIcon());
        existingCategory.setParent(category.getParent());
        categoryRepository.save(existingCategory);
    }

    public void deleteCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalStateException("Category with ID " + id + " does not exist.");
        }
        categoryRepository.deleteById(id);
    }
}
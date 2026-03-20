package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.model.Category;
import com.example.xuandangwebbanhang.service.CartService;
import com.example.xuandangwebbanhang.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Inject danh mục gốc (có kèm children) và thông tin giỏ hàng vào mọi trang.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @ModelAttribute("menuCategories")
    public List<Category> menuCategories() {
        return categoryService.getRootCategories();
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount() {
        return cartService.getTotalItems();
    }
}

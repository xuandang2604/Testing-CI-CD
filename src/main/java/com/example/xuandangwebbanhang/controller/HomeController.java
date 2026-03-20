package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("discountProducts", productService.getDiscountProducts());
        model.addAttribute("giftProducts", productService.getGiftProducts());
        model.addAttribute("normalProducts", productService.getNormalProducts());
        return "home";
    }
}

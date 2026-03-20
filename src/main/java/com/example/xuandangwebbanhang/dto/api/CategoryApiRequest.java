package com.example.xuandangwebbanhang.dto.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryApiRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String icon;
    // Frontend currently sends description; we map it to icon if icon is empty.
    private String description;
    private Long parentId;
}

package com.example.xuandangwebbanhang.dto.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductApiRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be >= 0")
    private Double price;

    private String description;
    private String image;
    private String promotionType;
    private Double discountPercent;
    private String giftDescription;
    private Integer promotionStockQuantity;
    private Long categoryId;
}


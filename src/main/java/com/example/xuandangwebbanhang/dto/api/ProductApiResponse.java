package com.example.xuandangwebbanhang.dto.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductApiResponse {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String image;
    private String promotionType;
    private Double discountPercent;
    private String giftDescription;
    private Integer promotionStockQuantity;
    private Long categoryId;
    private String categoryName;
}


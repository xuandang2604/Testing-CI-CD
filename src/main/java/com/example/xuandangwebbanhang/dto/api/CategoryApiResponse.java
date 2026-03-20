package com.example.xuandangwebbanhang.dto.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryApiResponse {
    private Long id;
    private String name;
    private String icon;
    private String description;
    private Long parentId;
    private String parentName;
}

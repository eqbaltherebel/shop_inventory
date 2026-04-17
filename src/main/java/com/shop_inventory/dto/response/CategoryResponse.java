package com.shop_inventory.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private int itemCount;    // how many items belong to this category
}

package com.shop_inventory.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ItemResponse {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private Double buyingPrice;
    private Double sellingPrice;
    private String locationDisplay; // "Aisle A · Rack 1 · Top"
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

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
    private Long locationId;        // ← ADD — needed for edit dropdown
    private String categoryName;
    private String photoUrl;       // direct Cloudinary URL
    private String photoPublicId;  // for frontend delete if needed
    private Long categoryId;        // ← ADD — needed for edit dropdown
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

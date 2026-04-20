package com.shop_inventory.dto.response;

import lombok.Data;

@Data
public class SaleItemResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private String itemPhotoUrl;
    private Integer quantitySold;
    private Double priceAtSale;
    private Double costAtSale;
    private Double subtotal;
    private Double profit;
}

package com.shop_inventory.dto.response;

import lombok.Data;

@Data
public class TopItemResponse {
    private Long itemId;
    private String itemName;
    private String itemPhotoUrl;
    private Integer totalQuantitySold;
    private Double totalRevenue;
    private Double totalProfit;
}

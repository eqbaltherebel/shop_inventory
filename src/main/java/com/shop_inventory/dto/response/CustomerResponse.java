package com.shop_inventory.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String email;

    // Summary stats
    private Integer totalPurchases;
    private Double totalSpent;
    private Double totalProfit;
    private LocalDateTime lastPurchase;
    private LocalDateTime createdAt;

    // Full sale history
    private List<SaleResponse> sales;
}

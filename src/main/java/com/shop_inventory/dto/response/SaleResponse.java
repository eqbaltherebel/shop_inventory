package com.shop_inventory.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleResponse {
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private String customerPhone;
    private Double totalAmount;
    private Double totalCost;
    private Double profit;
    private String paymentMethod;
    private String notes;
    private String status;
    private String soldBy;
    private LocalDateTime saleDate;
    private List<SaleItemResponse> saleItems;
}

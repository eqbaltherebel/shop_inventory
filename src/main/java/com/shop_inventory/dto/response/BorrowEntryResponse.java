package com.shop_inventory.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BorrowEntryResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private Double totalAmount;
    private Double amountPaid;
    private Double remainingBalance;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String description;
    private String notes;
    private String tags;
    private Double creditLimit;
    private String status;
    private boolean overdue;
    private boolean deleted;
    private List<BorrowPaymentResponse> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

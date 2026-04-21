package com.shop_inventory.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowPaymentResponse {
    private Long id;
    private Double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String notes;
    private boolean deleted;
    private LocalDateTime createdAt;
}

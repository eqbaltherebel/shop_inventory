package com.shop_inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowPaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String paymentMethod;
    private String notes;
}

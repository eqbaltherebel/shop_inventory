package com.shop_inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowEntryRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private Double totalAmount;

    @NotNull(message = "Borrow date is required")
    private LocalDate borrowDate;

    private LocalDate dueDate;
    private String description;
    private String notes;
    private String tags;
    private Double creditLimit;
}

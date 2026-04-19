package com.shop_inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class SaleRequest {

    // Optional — link to existing customer
    private Long customerId;

    private String customerName;
    private String customerPhone;
    private String customerAddress;

    @NotEmpty(message = "At least one item is required")
    private List<SaleItemRequest> items;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;   // CASH, UPI, CARD

    private String notes;
}

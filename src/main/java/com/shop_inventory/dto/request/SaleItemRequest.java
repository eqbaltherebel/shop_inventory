package com.shop_inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaleItemRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

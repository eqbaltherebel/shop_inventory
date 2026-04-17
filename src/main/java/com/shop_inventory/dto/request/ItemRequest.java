package com.shop_inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ItemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    private Double buyingPrice;

    @NotNull
    private Double sellingPrice;

    private Long locationId;
    private Long categoryId;
}
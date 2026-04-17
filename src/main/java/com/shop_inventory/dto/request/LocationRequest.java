package com.shop_inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {

    @NotBlank(message = "Aisle is required")
    private String aisle;

    @NotBlank(message = "Rack is required")
    private String rack;

    @NotBlank(message = "Shelf is required")
    private String shelf;

    private String notes;
}

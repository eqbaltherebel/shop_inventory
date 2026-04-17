package com.shop_inventory.dto.response;

import lombok.Data;

@Data
public class LocationResponse {
    private Long id;
    private String aisle;
    private String rack;
    private String shelf;
    private String notes;
    private String display;      // "Aisle A · Rack 1 · Top"
    private int itemCount;       // how many items are stored here
}

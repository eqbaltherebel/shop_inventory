package com.shop_inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class AuthResponse {
    private String token;
    private String username;
    private String role;

    AuthResponse(){}
}

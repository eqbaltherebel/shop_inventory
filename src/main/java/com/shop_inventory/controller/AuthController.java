package com.shop_inventory.controller;

import com.shop_inventory.dto.request.LoginRequest;
import com.shop_inventory.dto.response.AuthResponse;
import com.shop_inventory.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("In login");
            System.out.println("username " + request.getUsername());

            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            System.err.println("Login error: " + ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong during login");
        }
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody LoginRequest request) {
        return authService.register(request);
    }
}

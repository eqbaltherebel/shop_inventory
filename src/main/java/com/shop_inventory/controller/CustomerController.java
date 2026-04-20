package com.shop_inventory.controller;

import com.shop_inventory.dto.request.CustomerRequest;
import com.shop_inventory.dto.response.CustomerResponse;
import com.shop_inventory.service.CustomerService;
import com.shop_inventory.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SaleService saleService;

    @GetMapping
    public List<CustomerResponse> getAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    // GET /api/customers/search?query=rahul
    @GetMapping("/search")
    public List<CustomerResponse> search(
            @RequestParam String query) {
        return customerService.search(query);
    }

    // GET /api/customers/{id}/sales
    @GetMapping("/{id}/sales")
    public CustomerResponse getSalesHistory(
            @PathVariable Long id) {
        return customerService.getWithSales(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @RequestBody @Valid CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.create(request));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

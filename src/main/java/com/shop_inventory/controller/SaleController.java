package com.shop_inventory.controller;

import com.shop_inventory.dto.request.SaleRequest;
import com.shop_inventory.dto.response.ReportResponse;
import com.shop_inventory.dto.response.SaleResponse;
import com.shop_inventory.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    // POST /api/sales — create a new sale
    @PostMapping
    public ResponseEntity<SaleResponse> create(
            @RequestBody @Valid SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSale(request));
    }

    // GET /api/sales — all sales
    @GetMapping
    public List<SaleResponse> getAll() {
        return saleService.getAll();
    }

    // GET /api/sales/{id}
    @GetMapping("/{id}")
    public SaleResponse getById(@PathVariable Long id) {
        return saleService.getById(id);
    }

    // PUT /api/sales/{id}/cancel
    @PutMapping("/{id}/cancel")
    public SaleResponse cancel(@PathVariable Long id) {
        return saleService.cancelSale(id);
    }

    // GET /api/sales/report/today
    @GetMapping("/report/today")
    public ReportResponse todayReport() {
        return saleService.getTodayReport();
    }

    // GET /api/sales/report?from=2026-04-01&to=2026-04-19
    @GetMapping("/report")
    public ReportResponse report(
            @RequestParam String from,
            @RequestParam String to) {
        return saleService.getReport(from, to);
    }
}

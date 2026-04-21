package com.shop_inventory.controller;

import com.shop_inventory.dto.request.BorrowEntryRequest;
import com.shop_inventory.dto.request.BorrowPaymentRequest;
import com.shop_inventory.dto.response.BorrowEntryResponse;
import com.shop_inventory.dto.response.BorrowSummaryResponse;
import com.shop_inventory.dto.response.LedgerResponse;
import com.shop_inventory.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // ── Borrow Entries ────────────────────────────────────────

    @GetMapping
    public List<BorrowEntryResponse> getAll() {
        return borrowService.getAll();
    }

    @GetMapping("/{id}")
    public BorrowEntryResponse getById(@PathVariable Long id) {
        return borrowService.getById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<BorrowEntryResponse> getByCustomer(
            @PathVariable Long customerId) {
        return borrowService.getByCustomer(customerId);
    }

    @GetMapping("/search")
    public List<BorrowEntryResponse> search(
            @RequestParam String query) {
        return borrowService.search(query);
    }

    @GetMapping("/overdue")
    public List<BorrowEntryResponse> getOverdue() {
        return borrowService.getOverdue();
    }

    @GetMapping("/filter")
    public List<BorrowEntryResponse> filterByDate(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate to) {
        return borrowService.filterByDate(from, to);
    }

    @GetMapping("/summary")
    public BorrowSummaryResponse getSummary() {
        return borrowService.getSummary();
    }

    @GetMapping("/ledger/{customerId}")
    public LedgerResponse getLedger(
            @PathVariable Long customerId) {
        return borrowService.getLedger(customerId);
    }

    @PostMapping
    public ResponseEntity<BorrowEntryResponse> create(
            @RequestBody @Valid BorrowEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowService.createEntry(req));
    }

    @PutMapping("/{id}")
    public BorrowEntryResponse update(
            @PathVariable Long id,
            @RequestBody @Valid BorrowEntryRequest req) {
        return borrowService.updateEntry(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Deleted by user")
            String reason) {
        borrowService.softDeleteEntry(id, reason);
        return ResponseEntity.noContent().build();
    }

    // ── Payments ──────────────────────────────────────────────

    @PostMapping("/{entryId}/payments")
    public BorrowEntryResponse addPayment(
            @PathVariable Long entryId,
            @RequestBody @Valid BorrowPaymentRequest req) {
        return borrowService.addPayment(entryId, req);
    }

    @PutMapping("/payments/{paymentId}")
    public BorrowEntryResponse updatePayment(
            @PathVariable Long paymentId,
            @RequestBody @Valid BorrowPaymentRequest req) {
        return borrowService.updatePayment(paymentId, req);
    }

    @DeleteMapping("/payments/{paymentId}")
    public BorrowEntryResponse deletePayment(
            @PathVariable Long paymentId) {
        return borrowService.deletePayment(paymentId);
    }
}

package com.shop_inventory.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class LedgerResponse {
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private Double totalBorrowed;
    private Double totalPaid;
    private Double outstanding;
    private List<LedgerEntry> entries;

    @Data
    public static class LedgerEntry {
        private LocalDate date;
        private String type;       // BORROW or PAYMENT
        private String description;
        private Double amount;
        private Double paid;
        private Double balance;    // running balance
        private String notes;
        private Long referenceId;  // borrow or payment id
    }
}

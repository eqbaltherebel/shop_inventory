package com.shop_inventory.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class BorrowSummaryResponse {
    // Overall dashboard totals
    private Double totalCreditGiven;
    private Double totalCollected;
    private Double totalOutstanding;
    private Integer totalEntries;
    private Integer pendingEntries;
    private Integer overdueEntries;
    private Integer clearedEntries;

    // Per-customer summary
    private List<CustomerCreditSummary> customerSummaries;

    @Data
    public static class CustomerCreditSummary {
        private Long customerId;
        private String customerName;
        private String customerPhone;
        private Double totalBorrowed;
        private Double totalPaid;
        private Double outstanding;
        private Double creditLimit;
        private boolean creditLimitExceeded;
        private String status;      // CLEAR, WARNING, OVERDUE
    }
}

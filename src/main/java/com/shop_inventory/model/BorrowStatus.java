package com.shop_inventory.model;

public enum BorrowStatus {
    PENDING,    // still owes money
    PARTIAL,    // partially paid
    CLEARED,    // fully paid
    OVERDUE,    // past due date and not cleared
    WRITTEN_OFF // marked as bad debt
}

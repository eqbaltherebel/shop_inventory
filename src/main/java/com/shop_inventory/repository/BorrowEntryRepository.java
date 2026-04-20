package com.shop_inventory.repository;

import com.shop_inventory.model.BorrowEntry;
import com.shop_inventory.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BorrowEntryRepository
        extends JpaRepository<BorrowEntry, Long> {

    // All active (non-deleted) entries
    List<BorrowEntry> findByDeletedFalseOrderByBorrowDateDesc();

    // By customer
    List<BorrowEntry> findByCustomerIdAndDeletedFalse(Long customerId);

    // By status
    List<BorrowEntry> findByStatusAndDeletedFalse(BorrowStatus status);

    // Overdue — past due date and not cleared
    @Query("""
        SELECT b FROM BorrowEntry b
        WHERE b.dueDate < :today
        AND b.status NOT IN ('CLEARED','WRITTEN_OFF')
        AND b.deleted = false
        ORDER BY b.dueDate ASC
        """)
    List<BorrowEntry> findOverdue(@Param("today") LocalDate today);

    // Search by customer name, phone, address
    @Query("""
        SELECT b FROM BorrowEntry b
        JOIN b.customer c
        WHERE b.deleted = false AND (
          LOWER(c.name)    LIKE LOWER(CONCAT('%',:q,'%')) OR
          LOWER(c.phone)   LIKE LOWER(CONCAT('%',:q,'%')) OR
          LOWER(c.address) LIKE LOWER(CONCAT('%',:q,'%'))
        )
        ORDER BY b.borrowDate DESC
        """)
    List<BorrowEntry> searchByCustomer(@Param("q") String query);

    // Date range filter
    @Query("""
        SELECT b FROM BorrowEntry b
        WHERE b.deleted = false
        AND b.borrowDate BETWEEN :from AND :to
        ORDER BY b.borrowDate DESC
        """)
    List<BorrowEntry> findByDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Summary totals
    @Query("SELECT COALESCE(SUM(b.totalAmount),0) " +
            "FROM BorrowEntry b WHERE b.deleted=false")
    Double sumTotalCredit();

    @Query("SELECT COALESCE(SUM(b.amountPaid),0) " +
            "FROM BorrowEntry b WHERE b.deleted=false")
    Double sumTotalPaid();

    @Query("SELECT COALESCE(SUM(b.remainingBalance),0) " +
            "FROM BorrowEntry b WHERE b.deleted=false " +
            "AND b.status NOT IN ('CLEARED','WRITTEN_OFF')")
    Double sumOutstanding();
}

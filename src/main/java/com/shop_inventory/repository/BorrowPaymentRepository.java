package com.shop_inventory.repository;

import com.shop_inventory.model.BorrowPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BorrowPaymentRepository
        extends JpaRepository<BorrowPayment, Long> {

    List<BorrowPayment> findByBorrowEntryIdAndDeletedFalse(
            Long borrowEntryId);
}

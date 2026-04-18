package com.shop_inventory.repository;

import com.shop_inventory.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    // Top selling items in date range
    @Query("SELECT si FROM SaleItem si " +
            "WHERE si.sale.saleDate BETWEEN :from AND :to " +
            "AND si.sale.status = 'COMPLETED' " +
            "ORDER BY si.quantitySold DESC")
    List<SaleItem> findTopItemsInRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}

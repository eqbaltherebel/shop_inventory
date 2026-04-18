package com.shop_inventory.repository;

import com.shop_inventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    // Sales between date range
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(
            LocalDateTime from, LocalDateTime to);

    // Today's sales
    @Query("SELECT s FROM Sale s WHERE s.saleDate >= :startOfDay " +
            "AND s.saleDate <= :endOfDay ORDER BY s.saleDate DESC")
    List<Sale> findTodaySales(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Total revenue in range
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.saleDate BETWEEN :from AND :to " +
            "AND s.status = 'COMPLETED'")
    Double sumRevenueBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Total profit in range
    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Sale s " +
            "WHERE s.saleDate BETWEEN :from AND :to " +
            "AND s.status = 'COMPLETED'")
    Double sumProfitBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Count sales in range
    Long countBySaleDateBetween(LocalDateTime from, LocalDateTime to);
}

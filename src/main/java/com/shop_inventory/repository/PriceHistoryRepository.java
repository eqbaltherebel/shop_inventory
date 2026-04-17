package com.shop_inventory.repository;

import com.shop_inventory.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByItemIdOrderByChangedAtDesc(Long itemId);
}

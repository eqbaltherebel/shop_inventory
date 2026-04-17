package com.shop_inventory.repository;

import com.shop_inventory.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // Search by name or description
    @Query("SELECT i FROM Item i WHERE " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Item> searchItems(@Param("query") String query);

    // Low stock alert
    List<Item> findByQuantityLessThanEqual(int threshold);

    // Filter by category
    List<Item> findByCategoryId(Long categoryId);

    // Filter by location
    List<Item> findByLocationId(Long locationId);
}

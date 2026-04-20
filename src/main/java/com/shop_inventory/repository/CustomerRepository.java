package com.shop_inventory.repository;

import com.shop_inventory.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    // Search by name, phone, or address — case insensitive
    @Query("""
        SELECT c FROM Customer c WHERE
        LOWER(c.name)    LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(c.phone)   LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%')) OR
        LOWER(c.email)   LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY c.name ASC
        """)
    List<Customer> search(@Param("query") String query);
}

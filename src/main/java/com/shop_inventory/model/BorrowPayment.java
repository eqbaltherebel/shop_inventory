package com.shop_inventory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_entry_id", nullable = false)
    private BorrowEntry borrowEntry;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    private String paymentMethod;  // CASH, UPI, CARD
    private String notes;

    // Soft delete
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

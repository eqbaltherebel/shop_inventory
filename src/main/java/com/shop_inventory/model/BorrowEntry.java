package com.shop_inventory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "borrow_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private Double totalAmount;      // total credit given

    @Column(nullable = false)
    private Double amountPaid = 0.0; // total paid so far

    @Column(nullable = false)
    private Double remainingBalance; // totalAmount - amountPaid

    @Column(nullable = false)
    private LocalDate borrowDate;

    private LocalDate dueDate;       // optional due date

    private String description;      // what was taken on credit
    private String notes;
    private String tags;             // e.g. "trusted,regular"

    // Credit limit for this customer
    private Double creditLimit;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status = BorrowStatus.PENDING;

    // Soft delete — never hard delete
    private boolean deleted = false;
    private LocalDateTime deletedAt;
    private String deletedReason;

    @OneToMany(mappedBy = "borrowEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("paymentDate ASC")
    private List<BorrowPayment> payments;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

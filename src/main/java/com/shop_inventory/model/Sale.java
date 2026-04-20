package com.shop_inventory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;   // e.g. INV-20260419-001

    // ── Link to Customer entity ──────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String customerName;
    private String customerPhone;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double totalCost;       // total buying price (for profit calc)

    @Column(nullable = false)
    private Double profit;          // totalAmount - totalCost

    private String paymentMethod;   // CASH, UPI, CARD

    private String notes;

    @Enumerated(EnumType.STRING)
    private SaleStatus status = SaleStatus.COMPLETED;

    @OneToMany(mappedBy = "sale",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<SaleItem> saleItems;

    private String soldBy;          // username

    @CreationTimestamp
    private LocalDateTime saleDate;
}

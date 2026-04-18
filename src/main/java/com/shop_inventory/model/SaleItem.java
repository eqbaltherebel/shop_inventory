package com.shop_inventory.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sale_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer quantitySold;

    @Column(nullable = false)
    private Double priceAtSale;     // selling price at time of sale

    @Column(nullable = false)
    private Double costAtSale;      // buying price at time of sale

    @Column(nullable = false)
    private Double subtotal;        // quantitySold * priceAtSale

    @Column(nullable = false)
    private Double profit;          // (priceAtSale - costAtSale) * quantitySold
}

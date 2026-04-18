package com.shop_inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Buying price is required")
    @DecimalMin(value = "0.0", message = "Buying price cannot be negative")
    private Double buyingPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    private Double sellingPrice;

    @Column(length = 500)
    private String photoUrl;       // full Cloudinary HTTPS URL

    private String photoPublicId;  // e.g. hardware-shop/items/item_42
    // used to delete from Cloudinary


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
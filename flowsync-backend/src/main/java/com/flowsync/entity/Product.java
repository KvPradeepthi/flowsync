package com.flowsync.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity — core catalog item.
 *
 * warehouseLocation format: WH01-A-03
 *   └ WH01 = Warehouse number
 *   └ A    = Zone
 *   └ 03   = Rack/Bin
 *
 * reorderLevel triggers low-stock detection in LowStockScheduler.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    /**
     * When stockQuantity drops to or below this level,
     * the product is flagged as low stock.
     */
    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(length = 100)
    private String category;

    /**
     * Physical location inside the warehouse.
     * Example: WH01-A-03 (Warehouse 01, Zone A, Bin 03)
     */
    @Column(length = 50)
    private String warehouseLocation;

    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

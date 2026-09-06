package com.flowsync.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * WarehouseInventory entity — links a product to a specific warehouse facility.
 * Contains facility-specific stock levels and rack/bin location.
 */
@Entity
@Table(
    name = "warehouse_inventory",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_warehouse_product", columnNames = {"warehouse_id", "product_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(length = 50)
    private String rackBinLocation;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

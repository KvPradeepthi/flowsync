package com.flowsync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem entity — one line in an order.
 *
 * unitPrice is captured at the time of order placement
 * (never re-fetched later, so price changes don't affect historical orders).
 *
 * subtotal = unitPrice × quantity (calculated server-side).
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Price at time of purchase — snapshot, not live price.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * unitPrice × quantity — calculated server-side.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}

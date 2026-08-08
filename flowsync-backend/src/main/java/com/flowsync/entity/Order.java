package com.flowsync.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity — represents a customer purchase.
 *
 * orderStatus tracks the fulfilment lifecycle:
 *   PLACED → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *   PLACED / CONFIRMED → CANCELLED (inventory restored)
 *
 * pickStatus tracks the warehouse workflow:
 *   PENDING_PICK → PICKED → PACKED → (SHIPPED via orderStatus)
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PickStatus pickStatus = PickStatus.PENDING_PICK;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ─── Status Enums ──────────────────────────────────────────────────────────

    public enum OrderStatus {
        PLACED,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    public enum PickStatus {
        PENDING_PICK,
        PICKED,
        PACKED,
        SHIPPED   // mirrors orderStatus.SHIPPED
    }
}

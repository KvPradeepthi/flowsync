package com.flowsync.repository;

import com.flowsync.entity.Order;
import com.flowsync.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // All orders for a specific user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // All orders with a given status (admin use)
    List<Order> findByOrderStatus(OrderStatus status);

    // Count orders by status (dashboard)
    long countByOrderStatus(OrderStatus status);
}

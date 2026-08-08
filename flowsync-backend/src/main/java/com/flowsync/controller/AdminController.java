package com.flowsync.controller;

import com.flowsync.dto.request.ProductRequest;
import com.flowsync.dto.response.DashboardResponse;
import com.flowsync.dto.response.OrderResponse;
import com.flowsync.dto.response.ProductResponse;
import com.flowsync.entity.Order;
import com.flowsync.repository.OrderRepository;
import com.flowsync.repository.ProductRepository;
import com.flowsync.service.OrderService;
import com.flowsync.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints — all under /api/admin/**.
 * Protected at route level in SecurityConfig (.hasRole("ADMIN")).
 *
 * @PreAuthorize("hasRole('ADMIN')") provides method-level double protection.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // ─── Dashboard ──────────────────────────────────────────────────────────

    /**
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse dashboard = DashboardResponse.builder()
                .totalProducts(productRepository.count())
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByOrderStatus(Order.OrderStatus.PLACED)
                        + orderRepository.countByOrderStatus(Order.OrderStatus.CONFIRMED))
                .lowStockCount(productRepository.findLowStockProducts().size())
                .cancelledOrders(orderRepository.countByOrderStatus(Order.OrderStatus.CANCELLED))
                .deliveredOrders(orderRepository.countByOrderStatus(Order.OrderStatus.DELIVERED))
                .build();
        return ResponseEntity.ok(dashboard);
    }

    // ─── Products ───────────────────────────────────────────────────────────

    /**
     * GET /api/admin/products/low-stock
     */
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    /**
     * PUT /api/admin/inventory/{id}
     * Directly update stock quantity (e.g., after restocking).
     */
    @PutMapping("/inventory/{id}")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    // ─── Orders ─────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/orders
     * View all orders across all users.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * PUT /api/admin/orders/{id}/status?status=CONFIRMED
     * Update order status (CONFIRMED, PROCESSING, SHIPPED, DELIVERED).
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    /**
     * PUT /api/admin/orders/{id}/pick-status?pickStatus=PICKED
     * Update warehouse pick status.
     */
    @PutMapping("/orders/{id}/pick-status")
    public ResponseEntity<OrderResponse> updatePickStatus(
            @PathVariable Long id,
            @RequestParam Order.PickStatus pickStatus) {
        return ResponseEntity.ok(orderService.updatePickStatus(id, pickStatus));
    }
}

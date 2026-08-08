package com.flowsync.controller;

import com.flowsync.dto.request.OrderRequest;
import com.flowsync.dto.response.OrderResponse;
import com.flowsync.entity.Order;
import com.flowsync.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order endpoints for authenticated customers.
 *
 * @AuthenticationPrincipal injects the current user from the JWT SecurityContext.
 * This is how we know which user is making the request without passing userId in the body.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Place a new order. Stock validation + inventory deduction happen inside.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return new ResponseEntity<>(
                orderService.placeOrder(request, currentUser.getUsername()),
                HttpStatus.CREATED);
    }

    /**
     * GET /api/orders
     * List orders for the current logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(orderService.getOrdersForUser(currentUser.getUsername()));
    }

    /**
     * GET /api/orders/{id}
     * Get a specific order. Customers can only see their own orders.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(orderService.getOrderById(id, currentUser.getUsername()));
    }

    /**
     * DELETE /api/orders/{id}
     * Cancel an order (PLACED or CONFIRMED only). Inventory is restored.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(orderService.cancelOrder(id, currentUser.getUsername()));
    }
}

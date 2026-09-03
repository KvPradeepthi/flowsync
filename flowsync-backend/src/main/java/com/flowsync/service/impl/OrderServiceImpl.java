package com.flowsync.service.impl;

import com.flowsync.dto.request.OrderRequest;
import com.flowsync.dto.response.OrderResponse;
import com.flowsync.entity.Order;
import com.flowsync.entity.Order.OrderStatus;
import com.flowsync.entity.Order.PickStatus;
import com.flowsync.entity.OrderItem;
import com.flowsync.entity.Product;
import com.flowsync.entity.User;
import com.flowsync.exception.InsufficientStockException;
import com.flowsync.exception.OrderNotFoundException;
import com.flowsync.exception.ProductNotFoundException;
import com.flowsync.exception.UnauthorizedActionException;
import com.flowsync.repository.OrderRepository;
import com.flowsync.repository.ProductRepository;
import com.flowsync.repository.UserRepository;
import com.flowsync.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderService implementation.
 *
 * ─── Interview notes ───────────────────────────────────────────────────────
 *
 * @Transactional on placeOrder():
 *   The entire method runs inside one DB transaction.
 *   If InsufficientStockException is thrown at step 3, or any DB error
 *   occurs, the ENTIRE transaction is ROLLED BACK — no partial order is
 *   created, no stock is partially deducted. This guarantees data integrity.
 *
 * Why server-side total calculation?
 *   Never trust the client-sent price. We always fetch product.getPrice()
 *   from the database. A malicious client sending price=0 is ignored.
 *
 * cancelOrder() + inventory restoration:
 *   On cancellation, we restore stockQuantity for each item.
 *   This is also @Transactional — if the restore fails, the cancellation
 *   is also rolled back.
 * ───────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final com.flowsync.service.AuditLogService auditLogService;

    // ─── Place Order ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String userEmail) {
        // 1. Load the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        // 2. Build order items and validate stock
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // 2a. Validate product exists and acquire pessimistic write lock (SELECT ... FOR UPDATE)
            Product product = productRepository.findByIdForUpdate(itemReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemReq.getProductId()));

            // 2b. Check stock — InsufficientStockException triggers ROLLBACK
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(),
                        itemReq.getQuantity(),
                        product.getStockQuantity()
                );
            }

            // 2c. Get CURRENT price from DB (never trust client-side price)
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 2d. Deduct stock atomically inside this locked transaction
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            // 2e. Build OrderItem (no id yet — JPA will assign)
            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            orderItems.add(item);
        }

        // 3. Create the Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.PLACED)
                .pickStatus(PickStatus.PENDING_PICK)
                .orderItems(new ArrayList<>())
                .build();

        // Link items to the order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getOrderItems().add(item);
        }

        Order saved = orderRepository.save(order);

        auditLogService.log(
                user.getId(),
                userEmail,
                "ORDER_PLACED",
                "ORDER",
                saved.getId(),
                null,
                "TOTAL: " + totalAmount + ", STATUS: PLACED",
                "Order #" + saved.getId() + " placed with " + orderItems.size() + " item(s)"
        );

        log.info("Order #{} placed by {} — total: {}", saved.getId(), userEmail, totalAmount);

        return OrderResponse.from(saved);
    }

    // ─── Query ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Customers may only view their own orders; ADMIN and SALES can view any order
        if (!order.getUser().getEmail().equals(userEmail)) {
            User requestingUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException(userEmail));
            if (requestingUser.getRole() != com.flowsync.entity.User.Role.ADMIN
                    && requestingUser.getRole() != com.flowsync.entity.User.Role.SALES) {
                throw new UnauthorizedActionException("You are not authorized to view this order");
            }
        }

        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(userEmail));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // ─── Admin Status Updates ───────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus oldStatus = order.getOrderStatus();

        // Sync pickStatus when order is shipped
        if (newStatus == OrderStatus.SHIPPED) {
            order.setPickStatus(PickStatus.SHIPPED);
        }

        order.setOrderStatus(newStatus);
        Order updated = orderRepository.save(order);

        auditLogService.log(
                null,
                "SYSTEM",
                "ORDER_STATUS_UPDATE",
                "ORDER",
                updated.getId(),
                oldStatus.name(),
                newStatus.name(),
                "Order status updated from " + oldStatus + " to " + newStatus
        );

        log.info("Order #{} status → {}", id, newStatus);
        return OrderResponse.from(updated);
    }

    @Override
    @Transactional
    public OrderResponse updatePickStatus(Long id, PickStatus newPickStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setPickStatus(newPickStatus);
        Order updated = orderRepository.save(order);
        log.info("Order #{} pickStatus → {}", id, newPickStatus);
        return OrderResponse.from(updated);
    }

    // ─── Cancel + Restore Inventory ────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Verify ownership (or ADMIN / SALES)
        if (!order.getUser().getEmail().equals(userEmail)) {
            User requestingUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException(userEmail));
            if (requestingUser.getRole() != com.flowsync.entity.User.Role.ADMIN
                    && requestingUser.getRole() != com.flowsync.entity.User.Role.SALES) {
                throw new UnauthorizedActionException("You cannot cancel another user's order");
            }
        }

        // Only PLACED or CONFIRMED orders can be cancelled
        if (order.getOrderStatus() != OrderStatus.PLACED
                && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot cancel order in status: " + order.getOrderStatus());
        }

        // Restore inventory for each item
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
            log.info("Restored {} units of '{}' (product id={})",
                    item.getQuantity(), product.getName(), product.getId());
        }

        OrderStatus previousStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED);
        Order cancelled = orderRepository.save(order);

        auditLogService.log(
                null,
                userEmail,
                "ORDER_CANCELLED",
                "ORDER",
                cancelled.getId(),
                previousStatus.name(),
                "CANCELLED",
                "Order #" + id + " cancelled; inventory restored"
        );

        log.info("Order #{} CANCELLED — inventory restored", id);

        return OrderResponse.from(cancelled);
    }
}

package com.flowsync.service;

import com.flowsync.dto.request.OrderRequest;
import com.flowsync.dto.response.OrderResponse;
import com.flowsync.entity.Order;
import com.flowsync.entity.Order.OrderStatus;
import com.flowsync.entity.OrderItem;
import com.flowsync.entity.Product;
import com.flowsync.entity.User;
import com.flowsync.exception.InsufficientStockException;
import com.flowsync.exception.UnauthorizedActionException;
import com.flowsync.repository.OrderRepository;
import com.flowsync.repository.ProductRepository;
import com.flowsync.repository.UserRepository;
import com.flowsync.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        testProduct = Product.builder()
                .id(10L)
                .sku("PROD-LAPTOP-01")
                .name("ThinkPad X1 Carbon")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(10)
                .reorderLevel(3)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("placeOrder: atomic stock deduction and successful order placement")
    void placeOrder_success() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest(10L, 2);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemReq));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponse response = orderService.placeOrder(request, "jane@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(OrderStatus.PLACED, response.getOrderStatus());
        assertEquals(new BigDecimal("2599.98"), response.getTotalAmount());

        // Verify stock decremented from 10 to 8
        assertEquals(8, testProduct.getStockQuantity());
        verify(productRepository).save(testProduct);
        verify(orderRepository).save(any(Order.class));
        verify(auditLogService).log(eq(1L), eq("jane@example.com"), eq("ORDER_PLACED"), eq("ORDER"), eq(100L), any(), any(), any());
    }

    @Test
    @DisplayName("placeOrder: throws InsufficientStockException and rolls back without saving order")
    void placeOrder_insufficientStock_throwsException() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest(10L, 15);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemReq));

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testProduct));

        InsufficientStockException ex = assertThrows(
                InsufficientStockException.class,
                () -> orderService.placeOrder(request, "jane@example.com")
        );

        assertTrue(ex.getMessage().contains("ThinkPad X1 Carbon"));
        // Verify stock was NOT decremented
        assertEquals(10, testProduct.getStockQuantity());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("cancelOrder: restores stock and updates status to CANCELLED")
    void cancelOrder_success() {
        Order order = Order.builder()
                .id(200L)
                .user(testUser)
                .orderStatus(OrderStatus.PLACED)
                .totalAmount(new BigDecimal("1299.99"))
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(testProduct)
                .quantity(3)
                .unitPrice(testProduct.getPrice())
                .subtotal(testProduct.getPrice().multiply(BigDecimal.valueOf(3)))
                .build();
        order.getOrderItems().add(item);

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(200L, "jane@example.com");

        assertEquals(OrderStatus.CANCELLED, response.getOrderStatus());
        // Verify stock was restored: 10 + 3 = 13
        assertEquals(13, testProduct.getStockQuantity());
        verify(productRepository).save(testProduct);
        verify(auditLogService).log(isNull(), eq("jane@example.com"), eq("ORDER_CANCELLED"), eq("ORDER"), eq(200L), eq("PLACED"), eq("CANCELLED"), any());
    }

    @Test
    @DisplayName("cancelOrder: unauthorized customer cannot cancel other users orders")
    void cancelOrder_unauthorized_throwsException() {
        User otherUser = User.builder()
                .id(2L)
                .name("Bob Smith")
                .email("bob@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        Order order = Order.builder()
                .id(300L)
                .user(otherUser)
                .orderStatus(OrderStatus.PLACED)
                .build();

        when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedActionException.class, () -> orderService.cancelOrder(300L, "jane@example.com"));
        verify(orderRepository, never()).save(any(Order.class));
    }
}

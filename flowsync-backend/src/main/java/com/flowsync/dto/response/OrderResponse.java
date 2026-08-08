package com.flowsync.dto.response;

import com.flowsync.entity.Order;
import com.flowsync.entity.Order.OrderStatus;
import com.flowsync.entity.Order.PickStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private PickStatus pickStatus;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private String warehouseLocation;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .warehouseLocation(item.getProduct().getWarehouseLocation())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .pickStatus(order.getPickStatus())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

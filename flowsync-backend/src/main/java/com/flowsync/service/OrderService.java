package com.flowsync.service;

import com.flowsync.dto.request.OrderRequest;
import com.flowsync.dto.response.OrderResponse;
import com.flowsync.entity.Order;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request, String userEmail);

    OrderResponse getOrderById(Long id, String userEmail);

    List<OrderResponse> getOrdersForUser(String userEmail);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long id, Order.OrderStatus newStatus);

    OrderResponse updatePickStatus(Long id, Order.PickStatus newPickStatus);

    OrderResponse cancelOrder(Long id, String userEmail);
}

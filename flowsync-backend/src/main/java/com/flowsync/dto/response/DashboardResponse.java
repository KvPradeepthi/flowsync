package com.flowsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin dashboard summary statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long lowStockCount;
    private long cancelledOrders;
    private long deliveredOrders;
}

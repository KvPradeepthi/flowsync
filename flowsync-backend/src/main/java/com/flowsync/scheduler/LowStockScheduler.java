package com.flowsync.scheduler;

import com.flowsync.entity.Product;
import com.flowsync.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Low-stock monitoring scheduler.
 *
 * Runs every hour to check for products below their reorder level.
 * In a production system, this would send email/Slack alerts or
 * create purchase orders. For FlowSync, it logs warnings.
 *
 * Interview note:
 *   @Scheduled(fixedRate = 3600000) → runs every 3,600,000 ms = 1 hour.
 *   @EnableScheduling must be on the main application class.
 *   This demonstrates Spring's built-in task scheduling without external tools.
 *
 * NISSIN talking point:
 *   "I used Spring's @Scheduled annotation to implement automated inventory
 *    monitoring that periodically detects low-stock products and logs alerts —
 *    similar to how a smart factory monitors component levels."
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockScheduler {

    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 3_600_000) // every 1 hour
    public void checkLowStock() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        if (lowStockProducts.isEmpty()) {
            log.info("[LowStockScheduler] All products are adequately stocked.");
            return;
        }

        log.warn("[LowStockScheduler] ⚠ {} product(s) are LOW ON STOCK:", lowStockProducts.size());
        for (Product p : lowStockProducts) {
            log.warn("  → [{}] {} | Stock: {} | Reorder Level: {} | Location: {}",
                    p.getSku(),
                    p.getName(),
                    p.getStockQuantity(),
                    p.getReorderLevel(),
                    p.getWarehouseLocation()
            );
        }
    }
}

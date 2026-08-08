package com.flowsync.dto.response;

import com.flowsync.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private String category;
    private String warehouseLocation;
    private String imageUrl;
    private Boolean active;
    private boolean lowStock;   // computed: stockQuantity <= reorderLevel
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .reorderLevel(p.getReorderLevel())
                .category(p.getCategory())
                .warehouseLocation(p.getWarehouseLocation())
                .imageUrl(p.getImageUrl())
                .active(p.getActive())
                .lowStock(p.getStockQuantity() <= p.getReorderLevel())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

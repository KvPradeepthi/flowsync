package com.flowsync.dto.response;

import com.flowsync.entity.WarehouseInventory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInventoryResponse {
    private Long id;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String productSku;
    private String productName;
    private Integer quantity;
    private Integer reorderLevel;
    private String rackBinLocation;
    private LocalDateTime updatedAt;

    public static WarehouseInventoryResponse from(WarehouseInventory wi) {
        return WarehouseInventoryResponse.builder()
                .id(wi.getId())
                .warehouseId(wi.getWarehouse().getId())
                .warehouseCode(wi.getWarehouse().getCode())
                .warehouseName(wi.getWarehouse().getName())
                .productId(wi.getProduct().getId())
                .productSku(wi.getProduct().getSku())
                .productName(wi.getProduct().getName())
                .quantity(wi.getQuantity())
                .reorderLevel(wi.getReorderLevel())
                .rackBinLocation(wi.getRackBinLocation())
                .updatedAt(wi.getUpdatedAt())
                .build();
    }
}

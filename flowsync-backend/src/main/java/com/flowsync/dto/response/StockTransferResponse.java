package com.flowsync.dto.response;

import com.flowsync.entity.StockTransfer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferResponse {
    private Long id;
    private String transferNumber;
    private Long sourceWarehouseId;
    private String sourceWarehouseCode;
    private String sourceWarehouseName;
    private Long destinationWarehouseId;
    private String destinationWarehouseCode;
    private String destinationWarehouseName;
    private Long productId;
    private String productSku;
    private String productName;
    private Integer quantity;
    private StockTransfer.TransferStatus status;
    private String requestedByEmail;
    private String approvedByEmail;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StockTransferResponse from(StockTransfer st) {
        return StockTransferResponse.builder()
                .id(st.getId())
                .transferNumber(st.getTransferNumber())
                .sourceWarehouseId(st.getSourceWarehouse().getId())
                .sourceWarehouseCode(st.getSourceWarehouse().getCode())
                .sourceWarehouseName(st.getSourceWarehouse().getName())
                .destinationWarehouseId(st.getDestinationWarehouse().getId())
                .destinationWarehouseCode(st.getDestinationWarehouse().getCode())
                .destinationWarehouseName(st.getDestinationWarehouse().getName())
                .productId(st.getProduct().getId())
                .productSku(st.getProduct().getSku())
                .productName(st.getProduct().getName())
                .quantity(st.getQuantity())
                .status(st.getStatus())
                .requestedByEmail(st.getRequestedBy() != null ? st.getRequestedBy().getEmail() : null)
                .approvedByEmail(st.getApprovedBy() != null ? st.getApprovedBy().getEmail() : null)
                .notes(st.getNotes())
                .createdAt(st.getCreatedAt())
                .updatedAt(st.getUpdatedAt())
                .build();
    }
}

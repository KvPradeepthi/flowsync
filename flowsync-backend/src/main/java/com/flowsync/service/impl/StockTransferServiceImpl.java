package com.flowsync.service.impl;

import com.flowsync.dto.request.StockTransferRequest;
import com.flowsync.dto.response.StockTransferResponse;
import com.flowsync.entity.*;
import com.flowsync.entity.StockTransfer.TransferStatus;
import com.flowsync.exception.InsufficientStockException;
import com.flowsync.exception.ProductNotFoundException;
import com.flowsync.repository.*;
import com.flowsync.service.AuditLogService;
import com.flowsync.service.StockTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages the multi-warehouse stock transfer lifecycle:
 * PENDING -> APPROVED -> IN_TRANSIT -> COMPLETED
 * PENDING / APPROVED / IN_TRANSIT -> CANCELLED
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository transferRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public StockTransferResponse requestTransfer(StockTransferRequest request, String userEmail) {
        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new IllegalArgumentException("Source and destination warehouses cannot be the same");
        }

        User requester = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Warehouse source = warehouseRepository.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Source warehouse not found: " + request.getSourceWarehouseId()));

        Warehouse destination = warehouseRepository.findById(request.getDestinationWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Destination warehouse not found: " + request.getDestinationWarehouseId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        // Check source stock
        WarehouseInventory sourceInventory = inventoryRepository
                .findWithLockByWarehouseIdAndProductId(source.getId(), product.getId())
                .orElseThrow(() -> new InsufficientStockException(
                        product.getName() + " in " + source.getCode(), request.getQuantity(), 0));

        if (sourceInventory.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    product.getName() + " in " + source.getCode(),
                    request.getQuantity(),
                    sourceInventory.getQuantity()
            );
        }

        String transferNumber = "TRF-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        StockTransfer transfer = StockTransfer.builder()
                .transferNumber(transferNumber)
                .sourceWarehouse(source)
                .destinationWarehouse(destination)
                .product(product)
                .quantity(request.getQuantity())
                .status(TransferStatus.PENDING)
                .requestedBy(requester)
                .notes(request.getNotes())
                .build();

        StockTransfer saved = transferRepository.save(transfer);

        auditLogService.log(
                requester.getId(),
                userEmail,
                "STOCK_TRANSFER_REQUESTED",
                "STOCK_TRANSFER",
                saved.getId(),
                null,
                "STATUS: PENDING, QTY: " + saved.getQuantity(),
                "Requested transfer of " + saved.getQuantity() + " units from " + source.getCode() + " to " + destination.getCode()
        );

        log.info("Stock transfer #{} created by {}", saved.getTransferNumber(), userEmail);
        return StockTransferResponse.from(saved);
    }

    @Override
    @Transactional
    public StockTransferResponse approveTransfer(Long transferId, String approverEmail) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer cannot be approved from status: " + transfer.getStatus());
        }

        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Approver not found: " + approverEmail));

        transfer.setApprovedBy(approver);
        transfer.setStatus(TransferStatus.APPROVED);
        StockTransfer saved = transferRepository.save(transfer);

        auditLogService.log(
                approver.getId(),
                approverEmail,
                "STOCK_TRANSFER_APPROVED",
                "STOCK_TRANSFER",
                saved.getId(),
                "PENDING",
                "APPROVED",
                "Approved transfer #" + saved.getTransferNumber()
        );

        log.info("Stock transfer #{} approved by {}", saved.getTransferNumber(), approverEmail);
        return StockTransferResponse.from(saved);
    }

    @Override
    @Transactional
    public StockTransferResponse dispatchTransfer(Long transferId, String userEmail) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));

        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED transfers can be dispatched. Current status: " + transfer.getStatus());
        }

        WarehouseInventory sourceInventory = inventoryRepository
                .findWithLockByWarehouseIdAndProductId(transfer.getSourceWarehouse().getId(), transfer.getProduct().getId())
                .orElseThrow(() -> new InsufficientStockException("Source inventory not found", transfer.getQuantity(), 0));

        if (sourceInventory.getQuantity() < transfer.getQuantity()) {
            throw new InsufficientStockException(
                    transfer.getProduct().getName(), transfer.getQuantity(), sourceInventory.getQuantity());
        }

        int oldQty = sourceInventory.getQuantity();
        sourceInventory.setQuantity(oldQty - transfer.getQuantity());
        inventoryRepository.save(sourceInventory);

        transfer.setStatus(TransferStatus.IN_TRANSIT);
        StockTransfer saved = transferRepository.save(transfer);

        auditLogService.log(
                null,
                userEmail,
                "STOCK_TRANSFER_DISPATCHED",
                "STOCK_TRANSFER",
                saved.getId(),
                "APPROVED",
                "IN_TRANSIT",
                "Deducted " + saved.getQuantity() + " units from " + transfer.getSourceWarehouse().getCode()
        );

        log.info("Stock transfer #{} dispatched by {}", saved.getTransferNumber(), userEmail);
        return StockTransferResponse.from(saved);
    }

    @Override
    @Transactional
    public StockTransferResponse completeTransfer(Long transferId, String userEmail) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));

        if (transfer.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new IllegalStateException("Only IN_TRANSIT transfers can be completed. Current status: " + transfer.getStatus());
        }

        WarehouseInventory destInventory = inventoryRepository
                .findWithLockByWarehouseIdAndProductId(transfer.getDestinationWarehouse().getId(), transfer.getProduct().getId())
                .orElse(WarehouseInventory.builder()
                        .warehouse(transfer.getDestinationWarehouse())
                        .product(transfer.getProduct())
                        .quantity(0)
                        .reorderLevel(10)
                        .build());

        int oldQty = destInventory.getQuantity();
        destInventory.setQuantity(oldQty + transfer.getQuantity());
        inventoryRepository.save(destInventory);

        transfer.setStatus(TransferStatus.COMPLETED);
        StockTransfer saved = transferRepository.save(transfer);

        auditLogService.log(
                null,
                userEmail,
                "STOCK_TRANSFER_COMPLETED",
                "STOCK_TRANSFER",
                saved.getId(),
                "IN_TRANSIT",
                "COMPLETED",
                "Added " + saved.getQuantity() + " units to " + transfer.getDestinationWarehouse().getCode()
        );

        log.info("Stock transfer #{} completed by {}", saved.getTransferNumber(), userEmail);
        return StockTransferResponse.from(saved);
    }

    @Override
    @Transactional
    public StockTransferResponse cancelTransfer(Long transferId, String userEmail) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));

        if (transfer.getStatus() == TransferStatus.COMPLETED || transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel transfer in status: " + transfer.getStatus());
        }

        // If stock was already dispatched (IN_TRANSIT), restore to source warehouse
        if (transfer.getStatus() == TransferStatus.IN_TRANSIT) {
            WarehouseInventory sourceInventory = inventoryRepository
                    .findWithLockByWarehouseIdAndProductId(transfer.getSourceWarehouse().getId(), transfer.getProduct().getId())
                    .orElse(WarehouseInventory.builder()
                            .warehouse(transfer.getSourceWarehouse())
                            .product(transfer.getProduct())
                            .quantity(0)
                            .reorderLevel(10)
                            .build());

            sourceInventory.setQuantity(sourceInventory.getQuantity() + transfer.getQuantity());
            inventoryRepository.save(sourceInventory);
            log.info("Restored {} units back to source warehouse {}", transfer.getQuantity(), transfer.getSourceWarehouse().getCode());
        }

        TransferStatus oldStatus = transfer.getStatus();
        transfer.setStatus(TransferStatus.CANCELLED);
        StockTransfer saved = transferRepository.save(transfer);

        auditLogService.log(
                null,
                userEmail,
                "STOCK_TRANSFER_CANCELLED",
                "STOCK_TRANSFER",
                saved.getId(),
                oldStatus.name(),
                "CANCELLED",
                "Transfer cancelled by " + userEmail
        );

        log.info("Stock transfer #{} cancelled by {}", saved.getTransferNumber(), userEmail);
        return StockTransferResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransferResponse> getAllTransfers() {
        return transferRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(StockTransferResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferResponse getTransferById(Long id) {
        StockTransfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + id));
        return StockTransferResponse.from(transfer);
    }
}

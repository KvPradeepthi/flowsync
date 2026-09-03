package com.flowsync.service.impl;

import com.flowsync.dto.request.WarehouseInventoryRequest;
import com.flowsync.dto.request.WarehouseRequest;
import com.flowsync.dto.response.WarehouseInventoryResponse;
import com.flowsync.dto.response.WarehouseResponse;
import com.flowsync.entity.Product;
import com.flowsync.entity.Warehouse;
import com.flowsync.entity.WarehouseInventory;
import com.flowsync.exception.ProductNotFoundException;
import com.flowsync.repository.ProductRepository;
import com.flowsync.repository.WarehouseInventoryRepository;
import com.flowsync.repository.WarehouseRepository;
import com.flowsync.service.AuditLogService;
import com.flowsync.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Warehouse code already exists: " + request.getCode());
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .location(request.getLocation())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Created warehouse: {} [{}]", saved.getName(), saved.getCode());
        return WarehouseResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with id: " + id));
        return WarehouseResponse.from(warehouse);
    }

    @Override
    @Transactional
    public WarehouseInventoryResponse updateInventory(WarehouseInventoryRequest request, String userEmail) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with id: " + request.getWarehouseId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        WarehouseInventory inventory = inventoryRepository
                .findWithLockByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElse(WarehouseInventory.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .quantity(0)
                        .reorderLevel(10)
                        .build());

        int oldQty = inventory.getQuantity();
        int newQty = request.getQuantity();

        inventory.setQuantity(newQty);
        if (request.getReorderLevel() != null) {
            inventory.setReorderLevel(request.getReorderLevel());
        }
        if (request.getRackBinLocation() != null) {
            inventory.setRackBinLocation(request.getRackBinLocation());
        }

        WarehouseInventory saved = inventoryRepository.save(inventory);

        // Audit log recording before/after quantity
        auditLogService.log(
                null,
                userEmail,
                "INVENTORY_MANUAL_ADJUSTMENT",
                "WAREHOUSE_INVENTORY",
                saved.getId(),
                "quantity=" + oldQty,
                "quantity=" + newQty,
                "Updated stock for product " + product.getSku() + " in " + warehouse.getCode()
        );

        log.info("Updated inventory for warehouse {} product {}: {} -> {}",
                warehouse.getCode(), product.getSku(), oldQty, newQty);

        return WarehouseInventoryResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseInventoryResponse> getInventoryByWarehouse(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .map(WarehouseInventoryResponse::from)
                .collect(Collectors.toList());
    }
}

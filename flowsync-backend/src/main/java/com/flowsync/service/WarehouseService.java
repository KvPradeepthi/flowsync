package com.flowsync.service;

import com.flowsync.dto.request.WarehouseInventoryRequest;
import com.flowsync.dto.request.WarehouseRequest;
import com.flowsync.dto.response.WarehouseInventoryResponse;
import com.flowsync.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse createWarehouse(WarehouseRequest request);
    List<WarehouseResponse> getAllWarehouses();
    WarehouseResponse getWarehouseById(Long id);
    WarehouseInventoryResponse updateInventory(WarehouseInventoryRequest request, String userEmail);
    List<WarehouseInventoryResponse> getInventoryByWarehouse(Long warehouseId);
}

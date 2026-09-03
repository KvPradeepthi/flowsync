package com.flowsync.controller;

import com.flowsync.dto.request.WarehouseInventoryRequest;
import com.flowsync.dto.request.WarehouseRequest;
import com.flowsync.dto.response.WarehouseInventoryResponse;
import com.flowsync.dto.response.WarehouseResponse;
import com.flowsync.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return new ResponseEntity<>(warehouseService.createWarehouse(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PutMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<WarehouseInventoryResponse> updateInventory(
            @Valid @RequestBody WarehouseInventoryRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(warehouseService.updateInventory(request, currentUser.getUsername()));
    }

    @GetMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<WarehouseInventoryResponse>> getInventoryByWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getInventoryByWarehouse(id));
    }
}

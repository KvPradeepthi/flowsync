package com.flowsync.repository;

import com.flowsync.entity.WarehouseInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {

    Optional<WarehouseInventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    List<WarehouseInventory> findByWarehouseId(Long warehouseId);

    List<WarehouseInventory> findByProductId(Long productId);

    /**
     * Pessimistic Write Lock — prevents race conditions when concurrent operations
     * mutate inventory at a specific warehouse.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.product.id = :productId")
    Optional<WarehouseInventory> findWithLockByWarehouseIdAndProductId(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId);
}

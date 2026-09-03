package com.flowsync.repository;

import com.flowsync.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    Optional<StockTransfer> findByTransferNumber(String transferNumber);
    List<StockTransfer> findByStatus(StockTransfer.TransferStatus status);
    List<StockTransfer> findBySourceWarehouseId(Long warehouseId);
    List<StockTransfer> findByDestinationWarehouseId(Long warehouseId);
    List<StockTransfer> findAllByOrderByCreatedAtDesc();
}

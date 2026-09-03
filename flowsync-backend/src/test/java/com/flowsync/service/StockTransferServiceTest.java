package com.flowsync.service;

import com.flowsync.dto.request.StockTransferRequest;
import com.flowsync.dto.response.StockTransferResponse;
import com.flowsync.entity.*;
import com.flowsync.entity.StockTransfer.TransferStatus;
import com.flowsync.exception.InsufficientStockException;
import com.flowsync.repository.*;
import com.flowsync.service.impl.StockTransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceTest {

    @Mock
    private StockTransferRepository transferRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseInventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private StockTransferServiceImpl transferService;

    private Warehouse warehouseA;
    private Warehouse warehouseB;
    private Product productLaptop;
    private WarehouseInventory inventoryA;
    private User warehouseManager;

    @BeforeEach
    void setUp() {
        warehouseA = Warehouse.builder().id(1L).code("WH-ORD-01").name("Chicago Warehouse").active(true).build();
        warehouseB = Warehouse.builder().id(2L).code("WH-DFW-02").name("Dallas Warehouse").active(true).build();

        productLaptop = Product.builder()
                .id(10L)
                .sku("LAPTOP-001")
                .name("Enterprise Laptop 15")
                .price(new BigDecimal("1000.00"))
                .stockQuantity(100)
                .active(true)
                .build();

        inventoryA = WarehouseInventory.builder()
                .id(50L)
                .warehouse(warehouseA)
                .product(productLaptop)
                .quantity(100)
                .reorderLevel(10)
                .build();

        warehouseManager = User.builder()
                .id(3L)
                .name("Alex Manager")
                .email("alex@flowsync.com")
                .role(User.Role.WAREHOUSE_MANAGER)
                .build();
    }

    @Test
    @DisplayName("requestTransfer: creates transfer in PENDING status with audit log")
    void requestTransfer_success() {
        StockTransferRequest request = StockTransferRequest.builder()
                .sourceWarehouseId(1L)
                .destinationWarehouseId(2L)
                .productId(10L)
                .quantity(20)
                .notes("Rebalancing stock for Q3")
                .build();

        when(userRepository.findByEmail("alex@flowsync.com")).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouseB));
        when(productRepository.findById(10L)).thenReturn(Optional.of(productLaptop));
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 10L)).thenReturn(Optional.of(inventoryA));
        when(transferRepository.save(any(StockTransfer.class))).thenAnswer(inv -> {
            StockTransfer st = inv.getArgument(0);
            st.setId(500L);
            return st;
        });

        StockTransferResponse response = transferService.requestTransfer(request, "alex@flowsync.com");

        assertNotNull(response);
        assertEquals(TransferStatus.PENDING, response.getStatus());
        assertEquals(20, response.getQuantity());
        assertEquals("WH-ORD-01", response.getSourceWarehouseCode());
        assertEquals("WH-DFW-02", response.getDestinationWarehouseCode());
        verify(auditLogService).log(eq(3L), eq("alex@flowsync.com"), eq("STOCK_TRANSFER_REQUESTED"), eq("STOCK_TRANSFER"), eq(500L), any(), any(), any());
    }

    @Test
    @DisplayName("requestTransfer: throws InsufficientStockException if source warehouse has insufficient units")
    void requestTransfer_insufficientStock_throwsException() {
        StockTransferRequest request = StockTransferRequest.builder()
                .sourceWarehouseId(1L)
                .destinationWarehouseId(2L)
                .productId(10L)
                .quantity(150) // More than available 100
                .build();

        when(userRepository.findByEmail("alex@flowsync.com")).thenReturn(Optional.of(warehouseManager));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouseA));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouseB));
        when(productRepository.findById(10L)).thenReturn(Optional.of(productLaptop));
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 10L)).thenReturn(Optional.of(inventoryA));

        assertThrows(InsufficientStockException.class, () -> transferService.requestTransfer(request, "alex@flowsync.com"));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveTransfer: sets status to APPROVED and records approver")
    void approveTransfer_success() {
        StockTransfer transfer = StockTransfer.builder()
                .id(501L)
                .transferNumber("TRF-501")
                .sourceWarehouse(warehouseA)
                .destinationWarehouse(warehouseB)
                .product(productLaptop)
                .quantity(20)
                .status(TransferStatus.PENDING)
                .requestedBy(warehouseManager)
                .build();

        when(transferRepository.findById(501L)).thenReturn(Optional.of(transfer));
        when(userRepository.findByEmail("admin@flowsync.com")).thenReturn(Optional.of(
                User.builder().id(99L).email("admin@flowsync.com").role(User.Role.ADMIN).build()
        ));
        when(transferRepository.save(any(StockTransfer.class))).thenReturn(transfer);

        StockTransferResponse response = transferService.approveTransfer(501L, "admin@flowsync.com");

        assertEquals(TransferStatus.APPROVED, response.getStatus());
        assertEquals("admin@flowsync.com", response.getApprovedByEmail());
        verify(auditLogService).log(eq(99L), eq("admin@flowsync.com"), eq("STOCK_TRANSFER_APPROVED"), eq("STOCK_TRANSFER"), eq(501L), eq("PENDING"), eq("APPROVED"), any());
    }

    @Test
    @DisplayName("dispatchTransfer: decrements source warehouse stock and transitions to IN_TRANSIT")
    void dispatchTransfer_success() {
        StockTransfer transfer = StockTransfer.builder()
                .id(502L)
                .transferNumber("TRF-502")
                .sourceWarehouse(warehouseA)
                .destinationWarehouse(warehouseB)
                .product(productLaptop)
                .quantity(20)
                .status(TransferStatus.APPROVED)
                .build();

        when(transferRepository.findById(502L)).thenReturn(Optional.of(transfer));
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 10L)).thenReturn(Optional.of(inventoryA));
        when(transferRepository.save(any(StockTransfer.class))).thenReturn(transfer);

        StockTransferResponse response = transferService.dispatchTransfer(502L, "alex@flowsync.com");

        assertEquals(TransferStatus.IN_TRANSIT, response.getStatus());
        // Source warehouse stock decremented: 100 - 20 = 80
        assertEquals(80, inventoryA.getQuantity());
        verify(inventoryRepository).save(inventoryA);
    }

    @Test
    @DisplayName("completeTransfer: increments destination warehouse stock and transitions to COMPLETED")
    void completeTransfer_success() {
        StockTransfer transfer = StockTransfer.builder()
                .id(503L)
                .transferNumber("TRF-503")
                .sourceWarehouse(warehouseA)
                .destinationWarehouse(warehouseB)
                .product(productLaptop)
                .quantity(20)
                .status(TransferStatus.IN_TRANSIT)
                .build();

        WarehouseInventory inventoryB = WarehouseInventory.builder()
                .id(60L)
                .warehouse(warehouseB)
                .product(productLaptop)
                .quantity(15)
                .reorderLevel(10)
                .build();

        when(transferRepository.findById(503L)).thenReturn(Optional.of(transfer));
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(2L, 10L)).thenReturn(Optional.of(inventoryB));
        when(transferRepository.save(any(StockTransfer.class))).thenReturn(transfer);

        StockTransferResponse response = transferService.completeTransfer(503L, "alex@flowsync.com");

        assertEquals(TransferStatus.COMPLETED, response.getStatus());
        // Destination warehouse stock incremented: 15 + 20 = 35
        assertEquals(35, inventoryB.getQuantity());
        verify(inventoryRepository).save(inventoryB);
    }

    @Test
    @DisplayName("cancelTransfer: in-transit transfer restores stock back to source warehouse")
    void cancelTransfer_inTransit_restoresStock() {
        StockTransfer transfer = StockTransfer.builder()
                .id(504L)
                .transferNumber("TRF-504")
                .sourceWarehouse(warehouseA)
                .destinationWarehouse(warehouseB)
                .product(productLaptop)
                .quantity(20)
                .status(TransferStatus.IN_TRANSIT)
                .build();

        // Source currently at 80 because 20 was dispatched
        inventoryA.setQuantity(80);

        when(transferRepository.findById(504L)).thenReturn(Optional.of(transfer));
        when(inventoryRepository.findWithLockByWarehouseIdAndProductId(1L, 10L)).thenReturn(Optional.of(inventoryA));
        when(transferRepository.save(any(StockTransfer.class))).thenReturn(transfer);

        StockTransferResponse response = transferService.cancelTransfer(504L, "alex@flowsync.com");

        assertEquals(TransferStatus.CANCELLED, response.getStatus());
        // Restored back: 80 + 20 = 100
        assertEquals(100, inventoryA.getQuantity());
        verify(inventoryRepository).save(inventoryA);
    }
}

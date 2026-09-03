package com.flowsync.controller;

import com.flowsync.dto.request.StockTransferRequest;
import com.flowsync.dto.response.StockTransferResponse;
import com.flowsync.service.StockTransferService;
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
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping
    public ResponseEntity<StockTransferResponse> requestTransfer(
            @Valid @RequestBody StockTransferRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return new ResponseEntity<>(
                stockTransferService.requestTransfer(request, currentUser.getUsername()),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<StockTransferResponse> approveTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(stockTransferService.approveTransfer(id, currentUser.getUsername()));
    }

    @PutMapping("/{id}/dispatch")
    public ResponseEntity<StockTransferResponse> dispatchTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(stockTransferService.dispatchTransfer(id, currentUser.getUsername()));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<StockTransferResponse> completeTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(stockTransferService.completeTransfer(id, currentUser.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<StockTransferResponse> cancelTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(stockTransferService.cancelTransfer(id, currentUser.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<StockTransferResponse>> getAllTransfers() {
        return ResponseEntity.ok(stockTransferService.getAllTransfers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransferResponse> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.getTransferById(id));
    }
}

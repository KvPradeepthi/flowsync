package com.flowsync.service;

import com.flowsync.dto.request.StockTransferRequest;
import com.flowsync.dto.response.StockTransferResponse;

import java.util.List;

public interface StockTransferService {
    StockTransferResponse requestTransfer(StockTransferRequest request, String userEmail);
    StockTransferResponse approveTransfer(Long transferId, String approverEmail);
    StockTransferResponse dispatchTransfer(Long transferId, String userEmail);
    StockTransferResponse completeTransfer(Long transferId, String userEmail);
    StockTransferResponse cancelTransfer(Long transferId, String userEmail);
    List<StockTransferResponse> getAllTransfers();
    StockTransferResponse getTransferById(Long id);
}

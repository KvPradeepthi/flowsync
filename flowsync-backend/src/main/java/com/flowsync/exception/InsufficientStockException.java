package com.flowsync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when order quantity exceeds available stock.
 *
 * Interview note: This exception is caught in @Transactional order creation —
 * the entire transaction rolls back, restoring inventory consistency.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format(
                "Insufficient stock for '%s': requested %d, available %d",
                productName, requested, available
        ));
    }
}

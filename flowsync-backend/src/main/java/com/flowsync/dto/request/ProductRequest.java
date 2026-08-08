package com.flowsync.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be at most 50 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private String category;

    private String warehouseLocation;

    private String imageUrl;
}

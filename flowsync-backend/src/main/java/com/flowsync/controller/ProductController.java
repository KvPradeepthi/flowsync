package com.flowsync.controller;

import com.flowsync.dto.request.ProductRequest;
import com.flowsync.dto.response.ProductResponse;
import com.flowsync.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product endpoints.
 *
 * GET endpoints are public (configured in SecurityConfig).
 * POST / PUT / DELETE require ADMIN role (via /api/admin/products prefix).
 *
 * Interview note:
 *   @RestController = @Controller + @ResponseBody
 *   This means every method return value is serialized as JSON automatically.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * List all active products. Supports optional filters.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(search));
        }
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productService.getProductsByCategory(category));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * POST /api/products — ADMIN only (protected in SecurityConfig)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    /**
     * PUT /api/products/{id} — ADMIN only
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * DELETE /api/products/{id} — soft delete, ADMIN only
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

package com.flowsync.service.impl;

import com.flowsync.dto.request.ProductRequest;
import com.flowsync.dto.response.ProductResponse;
import com.flowsync.entity.Product;
import com.flowsync.exception.ProductNotFoundException;
import com.flowsync.repository.ProductRepository;
import com.flowsync.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService implementation.
 *
 * Interview notes:
 *   @Service marks this as a Spring-managed bean (business layer)
 *   @Transactional ensures DB operations are atomic
 *   @Slf4j provides a logger via Lombok
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalStateException("SKU already exists: " + request.getSku());
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .reorderLevel(request.getReorderLevel())
                .category(request.getCategory())
                .warehouseLocation(request.getWarehouseLocation())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product: {} (SKU: {})", saved.getName(), saved.getSku());
        return ProductResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setReorderLevel(request.getReorderLevel());
        product.setCategory(request.getCategory());
        product.setWarehouseLocation(request.getWarehouseLocation());
        product.setImageUrl(request.getImageUrl());

        Product updated = productRepository.save(product);
        log.info("Updated product id={}", id);
        return ProductResponse.from(updated);
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, int newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setStockQuantity(newQuantity);
        Product updated = productRepository.save(product);
        log.info("Updated stock for product id={}: {} units", id, newQuantity);
        return ProductResponse.from(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        // Soft delete — preserve historical order references
        product.setActive(false);
        productRepository.save(product);
        log.info("Soft-deleted product id={}", id);
    }
}

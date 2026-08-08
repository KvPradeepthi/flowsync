package com.flowsync.repository;

import com.flowsync.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by SKU (unique)
    Optional<Product> findBySku(String sku);

    // Only active products
    List<Product> findByActiveTrue();

    // Products by category (active only)
    List<Product> findByCategoryAndActiveTrue(String category);

    // Low-stock: stockQuantity <= reorderLevel
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= p.reorderLevel")
    List<Product> findLowStockProducts();

    // Search by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Check SKU uniqueness (useful in service layer)
    boolean existsBySku(String sku);
}

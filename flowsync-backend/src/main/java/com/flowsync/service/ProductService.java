package com.flowsync.service;

import com.flowsync.dto.request.ProductRequest;
import com.flowsync.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> searchProducts(String name);

    List<ProductResponse> getLowStockProducts();

    ProductResponse updateProduct(Long id, ProductRequest request);

    ProductResponse updateStock(Long id, int newQuantity);

    void deleteProduct(Long id);
}

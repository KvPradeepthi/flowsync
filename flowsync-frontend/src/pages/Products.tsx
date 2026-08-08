import React, { useEffect, useState } from 'react';
import { productService } from '../services/services';
import type { Product } from '../types';
import { useCart } from '../context/AppContext';

export default function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const { addToCart } = useCart();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await productService.getAll();
        setProducts(response.data);
      } catch (err) {
        setError('Failed to load products');
      } finally {
        setLoading(false);
      }
    };
    
    fetchProducts();
  }, []);

  if (loading) return <div className="loading-center"><div className="spinner"></div></div>;
  if (error) return <div className="container mt-3"><div className="alert alert-error">{error}</div></div>;

  return (
    <div className="container page">
      <div className="flex items-center justify-between mb-3">
        <h1 className="page-title" style={{ marginBottom: 0 }}>Product Catalog</h1>
        {/* Simple filters could go here */}
      </div>
      
      {products.length === 0 ? (
        <div className="card text-center text-muted">No products available at the moment.</div>
      ) : (
        <div className="products-grid">
          {products.map(product => (
            <div key={product.id} className="product-card">
              <div className="product-card-img">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <span>🛒</span>
                )}
              </div>
              <div className="product-card-body">
                <div className="product-sku">{product.sku}</div>
                <h3 className="product-name">{product.name}</h3>
                <div className="product-price">₹{product.price.toLocaleString('en-IN')}</div>
                
                <div className="flex items-center justify-between mt-2">
                  <div className={`product-stock ${product.lowStock ? 'text-danger' : ''}`}>
                    {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : 'Out of stock'}
                  </div>
                  {product.warehouseLocation && (
                    <div className="wh-location" title="Warehouse Location">
                      {product.warehouseLocation}
                    </div>
                  )}
                </div>
                
                <button 
                  className="btn btn-primary btn-full mt-2" 
                  disabled={product.stockQuantity <= 0}
                  onClick={() => addToCart(product, 1)}
                >
                  Add to Cart
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

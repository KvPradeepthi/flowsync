import { useEffect, useState } from 'react';
import { productService } from '../services/services';
import type { Product } from '../types';
import { useCart } from '../context/AppContext';

export default function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');
  const [filterStockOnly, setFilterStockOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const { addToCart } = useCart();

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await productService.getAll();
      setProducts(response.data);
    } catch (err) {
      setError('Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  const filteredProducts = products.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(search.toLowerCase()) || 
                          p.sku.toLowerCase().includes(search.toLowerCase()) ||
                          p.category.toLowerCase().includes(search.toLowerCase());
    const matchesStock = !filterStockOnly || p.stockQuantity > 0;
    return matchesSearch && matchesStock;
  });

  if (loading) return <div className="loading-center"><div className="spinner"></div></div>;
  if (error) return <div className="container mt-3"><div className="alert alert-error">{error}</div></div>;

  return (
    <div className="container page">
      <div className="flex items-center justify-between mb-3" style={{ flexWrap: 'wrap', gap: '1rem' }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>Product Catalog</h1>
        
        {/* Search & Stock Filter Toolbar */}
        <div className="flex gap-1 items-center" style={{ flexWrap: 'wrap', flex: 1, justifyContent: 'flex-end', maxWidth: '520px' }}>
          <input
            type="text"
            className="form-control"
            style={{ width: '220px', padding: '0.45rem 0.75rem', fontSize: '0.85rem' }}
            placeholder="Search name, SKU, category..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button
            type="button"
            className={`btn btn-sm ${!filterStockOnly ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilterStockOnly(false)}
          >
            All Items ({products.length})
          </button>
          <button
            type="button"
            className={`btn btn-sm ${filterStockOnly ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilterStockOnly(true)}
          >
            In Stock Only
          </button>
        </div>
      </div>
      
      {filteredProducts.length === 0 ? (
        <div className="card text-center text-muted" style={{ padding: '3rem 1rem' }}>
          <div style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>🔍</div>
          <div>No products match your search or filter criteria.</div>
          {(search || filterStockOnly) && (
            <button
              className="btn btn-sm btn-secondary mt-2"
              onClick={() => { setSearch(''); setFilterStockOnly(false); }}
            >
              Reset Filters
            </button>
          )}
        </div>
      ) : (
        <div className="products-grid">
          {filteredProducts.map(product => (
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
                  {product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

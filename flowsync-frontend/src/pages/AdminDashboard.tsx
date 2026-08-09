import { useEffect, useState } from 'react';
import { adminService, productService, orderService } from '../services/services';
import type { DashboardStats, Product, Order } from '../types';

export default function AdminDashboard() {
  const [tab, setTab] = useState<'dashboard' | 'products' | 'orders'>('dashboard');
  
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, [tab]);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (tab === 'dashboard') {
        const res = await adminService.getDashboard();
        setStats(res.data);
      } else if (tab === 'products') {
        const res = await productService.getAll();
        setProducts(res.data);
      } else if (tab === 'orders') {
        const res = await orderService.getAllOrders();
        setOrders(res.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStock = async (id: number, quantity: number) => {
    try {
      await productService.updateStock(id, quantity);
      fetchData();
    } catch (err) {
      alert('Failed to update stock');
    }
  };

  const handleOrderStatus = async (id: number, status: string) => {
    try {
      await orderService.updateStatus(id, status);
      fetchData();
    } catch (err) {
      alert('Failed to update order status');
    }
  };

  const handlePickStatus = async (id: number, pickStatus: string) => {
    try {
      await orderService.updatePickStatus(id, pickStatus);
      fetchData();
    } catch (err) {
      alert('Failed to update pick status');
    }
  };

  const handleAddProduct = async () => {
    const name = prompt('Enter product name:');
    if (!name) return;
    const price = prompt('Enter product price ($):', '99.99');
    if (!price || isNaN(Number(price))) return;
    const stock = prompt('Enter initial stock quantity:', '100');
    if (!stock || isNaN(Number(stock))) return;
    const sku = 'SKU-' + Math.floor(Math.random() * 10000);

    try {
      await productService.create({
        name,
        description: name,
        price: Number(price),
        stockQuantity: Number(stock),
        reorderLevel: 10,
        sku,
        category: 'General',
        warehouseLocation: 'WH-A1'
      });
      fetchData();
    } catch (err) {
      alert('Failed to add product');
    }
  };

  return (
    <div className="container page">
      <div className="flex items-center justify-between mb-3">
        <h1 className="page-title" style={{ marginBottom: 0 }}>Admin Portal</h1>
        <div className="flex gap-1">
          <button className={`btn ${tab === 'dashboard' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('dashboard')}>Dashboard</button>
          <button className={`btn ${tab === 'products' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('products')}>Products</button>
          <button className={`btn ${tab === 'orders' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('orders')}>Orders</button>
        </div>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner"></div></div>
      ) : (
        <>
          {/* Dashboard Tab */}
          {tab === 'dashboard' && stats && (
            <div className="dashboard-grid">
              <div className="stat-card">
                <div className="stat-number">{stats.totalProducts}</div>
                <div className="stat-label">Total Products</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">{stats.totalOrders}</div>
                <div className="stat-label">Total Orders</div>
              </div>
              <div className="stat-card">
                <div className="stat-number" style={{ color: 'var(--warning)' }}>{stats.pendingOrders}</div>
                <div className="stat-label">Pending Orders</div>
              </div>
              <div className="stat-card">
                <div className="stat-number" style={{ color: 'var(--danger)' }}>{stats.lowStockCount}</div>
                <div className="stat-label">Low Stock Items</div>
              </div>
              <div className="stat-card">
                <div className="stat-number" style={{ color: 'var(--success)' }}>{stats.deliveredOrders}</div>
                <div className="stat-label">Delivered</div>
              </div>
            </div>
          )}

          {/* Products Tab */}
          {tab === 'products' && (
            <div className="card">
              <div className="flex items-center justify-between mb-2">
                <h3 className="section-title" style={{ marginBottom: 0 }}>Inventory Management</h3>
                <button className="btn btn-sm btn-primary" onClick={handleAddProduct}>+ Add Product</button>
              </div>
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>SKU</th>
                      <th>Name</th>
                      <th>Location</th>
                      <th>Stock</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map(p => (
                      <tr key={p.id}>
                        <td style={{ fontFamily: 'monospace' }}>{p.sku}</td>
                        <td style={{ fontWeight: 600 }}>{p.name}</td>
                        <td><span className="wh-location">{p.warehouseLocation}</span></td>
                        <td>
                          <span className={p.lowStock ? 'text-danger' : ''} style={{ fontWeight: 600 }}>
                            {p.stockQuantity} {p.lowStock && '⚠'}
                          </span>
                        </td>
                        <td>
                          <button 
                            className="btn btn-sm btn-secondary"
                            onClick={() => {
                              const qty = prompt(`Update stock for ${p.name} (Current: ${p.stockQuantity}):`, String(p.stockQuantity));
                              if (qty !== null && !isNaN(Number(qty))) {
                                handleUpdateStock(p.id, Number(qty));
                              }
                            }}
                          >
                            Update Stock
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Orders Tab */}
          {tab === 'orders' && (
            <div className="card">
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Order ID</th>
                      <th>Customer</th>
                      <th>Status</th>
                      <th>Pick Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map(o => (
                      <tr key={o.id}>
                        <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>#{o.id}</td>
                        <td>{o.userName}</td>
                        <td>
                          <select 
                            className="form-control" 
                            style={{ padding: '0.3rem', fontSize: '0.8rem', width: 'auto' }}
                            value={o.orderStatus}
                            onChange={(e) => handleOrderStatus(o.id, e.target.value)}
                          >
                            <option value="PLACED">PLACED</option>
                            <option value="CONFIRMED">CONFIRMED</option>
                            <option value="PROCESSING">PROCESSING</option>
                            <option value="SHIPPED">SHIPPED</option>
                            <option value="DELIVERED">DELIVERED</option>
                            <option value="CANCELLED">CANCELLED</option>
                          </select>
                        </td>
                        <td>
                          <select 
                            className="form-control" 
                            style={{ padding: '0.3rem', fontSize: '0.8rem', width: 'auto' }}
                            value={o.pickStatus}
                            onChange={(e) => handlePickStatus(o.id, e.target.value)}
                          >
                            <option value="PENDING_PICK">PENDING PICK</option>
                            <option value="PICKED">PICKED</option>
                            <option value="PACKED">PACKED</option>
                            <option value="SHIPPED">SHIPPED</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}

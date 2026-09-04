import { useEffect, useState } from 'react';
import { adminService, productService, orderService, transferService, auditLogService } from '../services/services';
import type { DashboardStats, Product, Order, StockTransfer, AuditLog } from '../types';

export default function AdminDashboard() {
  const [tab, setTab] = useState<'dashboard' | 'products' | 'orders' | 'transfers' | 'audit-logs'>('dashboard');
  
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [transfers, setTransfers] = useState<StockTransfer[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
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
      } else if (tab === 'transfers') {
        const res = await transferService.getAll();
        setTransfers(res.data);
      } else if (tab === 'audit-logs') {
        const res = await auditLogService.getAll();
        setAuditLogs(res.data);
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

  const handleRequestTransfer = async () => {
    const src = prompt('Source Warehouse ID (e.g. 1):', '1');
    if (!src) return;
    const dst = prompt('Destination Warehouse ID (e.g. 2):', '2');
    if (!dst) return;
    const prod = prompt('Product ID to transfer (e.g. 1):', '1');
    if (!prod) return;
    const qty = prompt('Quantity to transfer:', '10');
    if (!qty || isNaN(Number(qty))) return;
    const notes = prompt('Transfer notes (optional):', 'Inter-warehouse stock rebalance');

    try {
      await transferService.request({
        sourceWarehouseId: Number(src),
        destinationWarehouseId: Number(dst),
        productId: Number(prod),
        quantity: Number(qty),
        notes: notes || undefined
      });
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to request stock transfer');
    }
  };

  const handleTransferAction = async (action: 'approve' | 'dispatch' | 'complete' | 'cancel', id: number) => {
    try {
      if (action === 'approve') await transferService.approve(id);
      else if (action === 'dispatch') await transferService.dispatch(id);
      else if (action === 'complete') await transferService.complete(id);
      else if (action === 'cancel') await transferService.cancel(id);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || `Failed to ${action} transfer`);
    }
  };

  return (
    <div className="container page">
      <div className="flex items-center justify-between mb-3" style={{ flexWrap: 'wrap', gap: '0.75rem' }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>Admin Portal</h1>
        <div className="flex gap-1" style={{ flexWrap: 'wrap' }}>
          <button className={`btn ${tab === 'dashboard' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('dashboard')}>Dashboard</button>
          <button className={`btn ${tab === 'products' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('products')}>Products</button>
          <button className={`btn ${tab === 'orders' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('orders')}>Orders</button>
          <button className={`btn ${tab === 'transfers' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('transfers')}>Transfers</button>
          <button className={`btn ${tab === 'audit-logs' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('audit-logs')}>Audit Logs</button>
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

          {/* Transfers Tab (Multi-Warehouse SCM) */}
          {tab === 'transfers' && (
            <div className="card">
              <div className="flex items-center justify-between mb-2">
                <div>
                  <h3 className="section-title" style={{ marginBottom: 0 }}>Multi-Warehouse Stock Transfers</h3>
                  <small style={{ color: 'var(--text-muted)' }}>Inter-facility transfer request, approval, dispatch & receipt state machine</small>
                </div>
                <button className="btn btn-sm btn-primary" onClick={handleRequestTransfer}>+ Request Transfer</button>
              </div>
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Transfer #</th>
                      <th>Source WH</th>
                      <th>Dest WH</th>
                      <th>Product</th>
                      <th>Qty</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transfers.length === 0 ? (
                      <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No stock transfers found</td></tr>
                    ) : (
                      transfers.map(t => (
                        <tr key={t.id}>
                          <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>{t.transferNumber}</td>
                          <td><span className="wh-location">{t.sourceWarehouseCode || `WH-${t.sourceWarehouseId}`}</span></td>
                          <td><span className="wh-location">{t.destinationWarehouseCode || `WH-${t.destinationWarehouseId}`}</span></td>
                          <td>{t.productName}</td>
                          <td style={{ fontWeight: 600 }}>{t.quantity}</td>
                          <td>
                            <span 
                              className={`badge ${
                                t.status === 'COMPLETED' ? 'badge-success' : 
                                t.status === 'CANCELLED' ? 'badge-danger' : 
                                t.status === 'IN_TRANSIT' ? 'badge-warning' : 'badge-secondary'
                              }`}
                              style={{ padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600 }}
                            >
                              {t.status}
                            </span>
                          </td>
                          <td>
                            <div className="flex gap-1" style={{ flexWrap: 'wrap' }}>
                              {t.status === 'PENDING' && (
                                <>
                                  <button className="btn btn-sm btn-primary" onClick={() => handleTransferAction('approve', t.id)}>Approve</button>
                                  <button className="btn btn-sm btn-secondary" onClick={() => handleTransferAction('cancel', t.id)}>Cancel</button>
                                </>
                              )}
                              {t.status === 'APPROVED' && (
                                <>
                                  <button className="btn btn-sm btn-primary" onClick={() => handleTransferAction('dispatch', t.id)}>Dispatch</button>
                                  <button className="btn btn-sm btn-secondary" onClick={() => handleTransferAction('cancel', t.id)}>Cancel</button>
                                </>
                              )}
                              {t.status === 'IN_TRANSIT' && (
                                <>
                                  <button className="btn btn-sm btn-primary" onClick={() => handleTransferAction('complete', t.id)}>Receive</button>
                                  <button className="btn btn-sm btn-secondary" onClick={() => handleTransferAction('cancel', t.id)}>Cancel</button>
                                </>
                              )}
                              {(t.status === 'COMPLETED' || t.status === 'CANCELLED') && (
                                <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Archived</span>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Audit Logs Tab */}
          {tab === 'audit-logs' && (
            <div className="card">
              <div className="mb-2">
                <h3 className="section-title" style={{ marginBottom: 0 }}>Immutable Audit Trail</h3>
                <small style={{ color: 'var(--text-muted)' }}>Complete historical traceability for inventory overrides, transfers, and order status events</small>
              </div>
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Action</th>
                      <th>Entity</th>
                      <th>Actor</th>
                      <th>Old Value</th>
                      <th>New Value</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.length === 0 ? (
                      <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No audit logs recorded yet</td></tr>
                    ) : (
                      auditLogs.map(log => (
                        <tr key={log.id}>
                          <td style={{ fontSize: '0.75rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                            {log.timestamp ? new Date(log.timestamp).toLocaleString() : '-'}
                          </td>
                          <td>
                            <span 
                              style={{ 
                                padding: '0.2rem 0.4rem', 
                                borderRadius: '4px', 
                                fontSize: '0.75rem', 
                                fontWeight: 600,
                                backgroundColor: 'rgba(59, 130, 246, 0.15)',
                                color: '#60a5fa'
                              }}
                            >
                              {log.action}
                            </span>
                          </td>
                          <td style={{ fontWeight: 600 }}>{log.entityType} #{log.entityId}</td>
                          <td style={{ fontSize: '0.85rem' }}>{log.userEmail}</td>
                          <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{log.oldValue || '-'}</td>
                          <td style={{ fontSize: '0.8rem', fontWeight: 600 }}>{log.newValue || '-'}</td>
                          <td style={{ fontSize: '0.8rem' }}>{log.details || '-'}</td>
                        </tr>
                      ))
                    )}
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

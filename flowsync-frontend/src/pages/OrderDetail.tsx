import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderService } from '../services/services';
import type { Order } from '../types';

export default function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const response = await orderService.getById(Number(id));
        setOrder(response.data);
      } catch (err) {
        setError('Failed to load order details');
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchOrder();
  }, [id]);

  const handleCancel = async () => {
    if (!order) return;
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    
    setCancelling(true);
    try {
      const response = await orderService.cancelOrder(order.id);
      setOrder(response.data);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel order');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <div className="loading-center"><div className="spinner"></div></div>;
  if (error || !order) return <div className="container page"><div className="alert alert-error">{error || 'Order not found'}</div></div>;

  const canCancel = order.orderStatus === 'PLACED' || order.orderStatus === 'CONFIRMED';

  return (
    <div className="container page">
      <div className="flex items-center justify-between mb-3">
        <h1 className="page-title" style={{ marginBottom: 0 }}>Order #{order.id}</h1>
        <button className="btn btn-secondary" onClick={() => navigate('/orders')}>Back to Orders</button>
      </div>
      
      <div className="form-row" style={{ gap: '2rem' }}>
        <div style={{ flex: '2' }}>
          <div className="card mb-3">
            <h3 className="section-title">Items</h3>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Item</th>
                    <th>Price</th>
                    <th>Qty</th>
                    <th>Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {order.items.map(item => (
                    <tr key={item.id}>
                      <td>
                        <div style={{ fontWeight: 600 }}>{item.productName}</div>
                        <div className="text-muted" style={{ fontSize: '0.8rem' }}>{item.productSku}</div>
                      </td>
                      <td>₹{item.unitPrice.toLocaleString('en-IN')}</td>
                      <td>{item.quantity}</td>
                      <td style={{ fontWeight: 600 }}>₹{item.subtotal.toLocaleString('en-IN')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div style={{ flex: '1' }}>
          <div className="card mb-3">
            <h3 className="section-title">Summary</h3>
            <div className="flex justify-between mb-2">
              <span className="text-muted">Date</span>
              <span>{new Date(order.createdAt).toLocaleString()}</span>
            </div>
            <div className="flex justify-between mb-3" style={{ fontSize: '1.25rem', fontWeight: 700, borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
              <span>Total</span>
              <span style={{ color: 'var(--accent-light)' }}>₹{order.totalAmount.toLocaleString('en-IN')}</span>
            </div>
            
            <div style={{ padding: '1rem', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)', marginBottom: '1rem' }}>
              <div className="flex justify-between items-center mb-2">
                <span className="text-muted">Order Status</span>
                <span className={`badge badge-${order.orderStatus.toLowerCase()}`}>{order.orderStatus}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-muted">Fulfillment</span>
                <span className={`badge badge-${order.pickStatus.toLowerCase()}`}>{order.pickStatus.replace('_', ' ')}</span>
              </div>
            </div>

            {canCancel && (
              <button 
                className="btn btn-danger btn-full" 
                onClick={handleCancel}
                disabled={cancelling}
              >
                {cancelling ? 'Cancelling...' : 'Cancel Order'}
              </button>
            )}
            {!canCancel && order.orderStatus !== 'CANCELLED' && (
              <p className="text-muted text-center" style={{ fontSize: '0.8rem' }}>
                This order is being processed and can no longer be cancelled.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart, useAuth } from '../context/AppContext';
import { orderService } from '../services/services';

export default function Cart() {
  const { cart, updateQuantity, removeFromCart, cartTotal, clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState('');

  const handlePlaceOrder = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    setPlacing(true);
    setError('');

    try {
      const items = cart.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }));
      
      const res = await orderService.placeOrder(items);
      clearCart();
      navigate(`/orders/${res.data.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to place order.');
    } finally {
      setPlacing(false);
    }
  };

  if (cart.length === 0) {
    return (
      <div className="container page text-center">
        <h1 className="page-title mt-3">Your Cart is Empty</h1>
        <button className="btn btn-primary mt-2" onClick={() => navigate('/')}>
          Browse Products
        </button>
      </div>
    );
  }

  return (
    <div className="container page">
      <h1 className="page-title">Shopping Cart</h1>
      
      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-row" style={{ gap: '2rem' }}>
        <div style={{ flex: '2' }}>
          <div className="card">
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Subtotal</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {cart.map((item) => (
                    <tr key={item.product.id}>
                      <td>
                        <div style={{ fontWeight: 600 }}>{item.product.name}</div>
                        <div className="text-muted" style={{ fontSize: '0.8rem' }}>{item.product.sku}</div>
                        {item.product.warehouseLocation && (
                          <div className="wh-location mt-1" style={{ fontSize: '0.7rem', padding: '0.1rem 0.4rem' }}>
                            {item.product.warehouseLocation}
                          </div>
                        )}
                      </td>
                      <td>₹{item.product.price.toLocaleString('en-IN')}</td>
                      <td>
                        <div className="flex items-center gap-1">
                          <button 
                            className="btn btn-secondary btn-sm" 
                            onClick={() => updateQuantity(item.product.id, item.quantity - 1)}
                          >-</button>
                          <span>{item.quantity}</span>
                          <button 
                            className="btn btn-secondary btn-sm"
                            disabled={item.quantity >= item.product.stockQuantity}
                            onClick={() => updateQuantity(item.product.id, item.quantity + 1)}
                          >+</button>
                        </div>
                      </td>
                      <td style={{ fontWeight: 600 }}>
                        ₹{(item.product.price * item.quantity).toLocaleString('en-IN')}
                      </td>
                      <td>
                        <button className="btn btn-danger btn-sm" onClick={() => removeFromCart(item.product.id)}>
                          Remove
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div style={{ flex: '1' }}>
          <div className="card">
            <h3 className="section-title">Order Summary</h3>
            <div className="flex justify-between mb-1 text-muted">
              <span>Items</span>
              <span>{cart.reduce((s, i) => s + i.quantity, 0)}</span>
            </div>
            <div className="flex justify-between mb-2 text-muted">
              <span>Shipping</span>
              <span>Calculated at next step</span>
            </div>
            <div className="flex justify-between mb-3" style={{ fontSize: '1.25rem', fontWeight: 700, borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
              <span>Total</span>
              <span>₹{cartTotal.toLocaleString('en-IN')}</span>
            </div>
            
            <button 
              className="btn btn-primary btn-full btn-lg" 
              onClick={handlePlaceOrder}
              disabled={placing}
            >
              {placing ? 'Processing...' : 'Place Order'}
            </button>
            {!user && (
              <p className="text-muted text-center mt-1" style={{ fontSize: '0.8rem' }}>
                You will be asked to sign in first.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

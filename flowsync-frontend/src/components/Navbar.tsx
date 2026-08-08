import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth, useCart } from '../context/AppContext';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="nav-brand">
          FlowSync
        </Link>

        <div className="nav-links">
          {user ? (
            <>
              <Link to="/" className="nav-link">Products</Link>
              <Link to="/orders" className="nav-link">My Orders</Link>
              
              {isAdmin && (
                <Link to="/admin" className="nav-link" style={{ color: 'var(--warning)' }}>
                  Dashboard
                </Link>
              )}
              
              <Link to="/cart" className="nav-link">
                Cart {cartCount > 0 && <span className="nav-badge">{cartCount}</span>}
              </Link>
              
              <div style={{ width: '1px', height: '24px', background: 'var(--border)', margin: '0 8px' }}></div>
              
              <span className="nav-link" style={{ cursor: 'default' }}>
                <span className="text-muted" style={{ marginRight: '4px' }}>Hi,</span> 
                {user.name.split(' ')[0]}
              </span>
              <button onClick={handleLogout} className="btn btn-sm btn-secondary" style={{ marginLeft: '4px' }}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">Sign In</Link>
              <Link to="/register" className="btn btn-sm btn-primary">Sign Up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

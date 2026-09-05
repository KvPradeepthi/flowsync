
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth, useCart } from '../context/AppContext';
import { authService } from '../services/services';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();

  // Change password modal state
  const [showModal, setShowModal] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passError, setPassError] = useState('');
  const [passSuccess, setPassSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPassError('');
    setPassSuccess('');

    if (newPassword.length < 6) {
      setPassError('New password must be at least 6 characters.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setPassError('New passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const res = await authService.changePassword(currentPassword, newPassword);
      setPassSuccess(res.data?.message || 'Password updated successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setTimeout(() => {
        setShowModal(false);
        setPassSuccess('');
      }, 1500);
    } catch (err: any) {
      setPassError(err.response?.data?.message || 'Failed to update password. Verify your current password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
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

                <button
                  onClick={() => {
                    setShowModal(true);
                    setPassError('');
                    setPassSuccess('');
                  }}
                  className="btn btn-sm btn-secondary"
                  title="Change Password"
                  style={{ fontSize: '0.75rem', padding: '0.3rem 0.6rem' }}
                >
                  🔒 Key
                </button>

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

      {/* Change Password Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => !loading && setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-2">
              <h2 style={{ fontSize: '1.25rem', fontWeight: 600 }}>Change Password</h2>
              <button
                type="button"
                onClick={() => setShowModal(false)}
                disabled={loading}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.25rem', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
              Update password for <strong>{user?.email}</strong>
            </p>

            {passError && <div className="alert alert-error">{passError}</div>}
            {passSuccess && <div className="alert alert-success">{passSuccess}</div>}

            <form onSubmit={handlePasswordSubmit}>
              <div className="form-group">
                <label className="form-label">Current Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="Enter current password"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">New Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="At least 6 characters"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Confirm New Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                  required
                />
              </div>

              <div className="flex justify-between gap-1 mt-3">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowModal(false)}
                  disabled={loading}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Saving...' : 'Update Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

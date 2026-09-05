import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/services';
import { useAuth } from '../context/AppContext';

export default function Login() {
  const [mode, setMode] = useState<'login' | 'forgot'>('login');
  
  // Login fields
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  
  // Reset password fields
  const [resetEmail, setResetEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const response = await authService.login(email, password);
      login(response.data);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (newPassword.length < 6) {
      setError('New password must be at least 6 characters');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      const res = await authService.resetPassword(resetEmail, newPassword);
      setSuccess(res.data?.message || 'Password reset successfully. You can now sign in.');
      setEmail(resetEmail);
      setPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setTimeout(() => {
        setMode('login');
      }, 1500);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to reset password. Please check your email.');
    } finally {
      setLoading(false);
    }
  };

  const fillDemoCredentials = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError('');
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        {mode === 'login' ? (
          <>
            <h1 className="auth-title">Welcome back</h1>
            <p className="auth-subtitle">Sign in to your FlowSync account</p>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success" style={{ background: 'rgba(34, 197, 94, 0.15)', border: '1px solid var(--success)', color: 'var(--success)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', marginBottom: '1rem', fontSize: '0.9rem' }}>{success}</div>}

            <form onSubmit={handleLoginSubmit}>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  type="email"
                  className="form-control"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@company.com"
                  required
                />
              </div>

              <div className="form-group">
                <div className="flex justify-between items-center mb-1">
                  <label className="form-label" style={{ marginBottom: 0 }}>Password</label>
                  <button
                    type="button"
                    onClick={() => {
                      setMode('forgot');
                      setResetEmail(email);
                      setError('');
                      setSuccess('');
                    }}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--accent-light)',
                      fontSize: '0.8rem',
                      cursor: 'pointer',
                      padding: 0
                    }}
                  >
                    Forgot password?
                  </button>
                </div>
                <input
                  type="password"
                  className="form-control"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary btn-full btn-lg mt-1" disabled={loading}>
                {loading ? 'Signing in...' : 'Sign In'}
              </button>
            </form>

            {/* Quick Demo Access Buttons for Interviewers & Reviewers */}
            <div style={{ marginTop: '1.5rem', paddingTop: '1.25rem', borderTop: '1px solid var(--border)' }}>
              <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.5px', color: 'var(--text-muted)', marginBottom: '0.5rem', textAlign: 'center' }}>
                ⚡ Quick Demo Login (One-Click)
              </div>
              <div className="flex gap-1">
                <button
                  type="button"
                  className="btn btn-sm btn-secondary"
                  style={{ flex: 1, fontSize: '0.8rem' }}
                  onClick={() => fillDemoCredentials('admin@flowsync.com', 'admin123')}
                >
                  Fill Admin
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-secondary"
                  style={{ flex: 1, fontSize: '0.8rem' }}
                  onClick={() => fillDemoCredentials('jane@example.com', 'customer123')}
                >
                  Fill Customer
                </button>
              </div>
            </div>

            <p className="text-center text-muted mt-3" style={{ fontSize: '0.9rem' }}>
              Don't have an account? <Link to="/register">Register here</Link>
            </p>
          </>
        ) : (
          <>
            <h1 className="auth-title">Reset Password</h1>
            <p className="auth-subtitle">Enter your account email and new password</p>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success" style={{ background: 'rgba(34, 197, 94, 0.15)', border: '1px solid var(--success)', color: 'var(--success)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', marginBottom: '1rem', fontSize: '0.9rem' }}>{success}</div>}

            <form onSubmit={handleResetSubmit}>
              <div className="form-group">
                <label className="form-label">Account Email</label>
                <input
                  type="email"
                  className="form-control"
                  value={resetEmail}
                  onChange={(e) => setResetEmail(e.target.value)}
                  placeholder="name@company.com"
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
                  placeholder="Re-enter your new password"
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary btn-full btn-lg mt-1" disabled={loading}>
                {loading ? 'Resetting Password...' : 'Set New Password'}
              </button>
            </form>

            <p className="text-center text-muted mt-3" style={{ fontSize: '0.9rem' }}>
              Remembered your password?{' '}
              <button
                type="button"
                onClick={() => {
                  setMode('login');
                  setError('');
                  setSuccess('');
                }}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--accent-light)',
                  cursor: 'pointer',
                  padding: 0
                }}
              >
                Back to Sign In
              </button>
            </p>
          </>
        )}
      </div>
    </div>
  );
}

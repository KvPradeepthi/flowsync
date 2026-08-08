import React, { createContext, useContext, useState, useCallback } from 'react';
import type { AuthResponse, CartItem, Product } from '../types';

// ─── Auth Context ─────────────────────────────────────────────────────────

interface AuthContextType {
  user: AuthResponse | null;
  login: (data: AuthResponse) => void;
  logout: () => void;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

// ─── Cart Context ─────────────────────────────────────────────────────────

interface CartContextType {
  cart: CartItem[];
  addToCart: (product: Product, quantity: number) => void;
  removeFromCart: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  cartTotal: number;
  cartCount: number;
}

const CartContext = createContext<CartContextType | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────

export function AppProvider({ children }: { children: React.ReactNode }) {
  // Initialize from localStorage for page-refresh persistence
  const [user, setUser] = useState<AuthResponse | null>(() => {
    const stored = localStorage.getItem('flowsync_user');
    return stored ? JSON.parse(stored) : null;
  });

  const [cart, setCart] = useState<CartItem[]>([]);

  const login = useCallback((data: AuthResponse) => {
    localStorage.setItem('flowsync_token', data.token);
    localStorage.setItem('flowsync_user', JSON.stringify(data));
    setUser(data);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('flowsync_token');
    localStorage.removeItem('flowsync_user');
    setUser(null);
    setCart([]);
  }, []);

  const addToCart = useCallback((product: Product, quantity: number) => {
    setCart((prev) => {
      const existing = prev.find((item) => item.product.id === product.id);
      if (existing) {
        return prev.map((item) =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      }
      return [...prev, { product, quantity }];
    });
  }, []);

  const removeFromCart = useCallback((productId: number) => {
    setCart((prev) => prev.filter((item) => item.product.id !== productId));
  }, []);

  const updateQuantity = useCallback((productId: number, quantity: number) => {
    if (quantity <= 0) {
      setCart((prev) => prev.filter((item) => item.product.id !== productId));
      return;
    }
    setCart((prev) =>
      prev.map((item) =>
        item.product.id === productId ? { ...item, quantity } : item
      )
    );
  }, []);

  const clearCart = useCallback(() => setCart([]), []);

  const cartTotal = cart.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );
  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin: user?.role === 'ADMIN' }}>
      <CartContext.Provider
        value={{ cart, addToCart, removeFromCart, updateQuantity, clearCart, cartTotal, cartCount }}
      >
        {children}
      </CartContext.Provider>
    </AuthContext.Provider>
  );
}

// ─── Hooks ────────────────────────────────────────────────────────────────

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AppProvider');
  return ctx;
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used inside AppProvider');
  return ctx;
}

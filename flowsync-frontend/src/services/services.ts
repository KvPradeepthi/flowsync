import api from './api';
import type { AuthResponse, Product, Order, DashboardStats } from '../types';

// ─── Auth ──────────────────────────────────────────────────────────────────

export const authService = {
  register: (name: string, email: string, password: string) =>
    api.post<AuthResponse>('/api/auth/register', { name, email, password }),

  login: (email: string, password: string) =>
    api.post<AuthResponse>('/api/auth/login', { email, password }),
};

// ─── Products ─────────────────────────────────────────────────────────────

export const productService = {
  getAll: (search?: string, category?: string) => {
    const params: Record<string, string> = {};
    if (search) params.search = search;
    if (category) params.category = category;
    return api.get<Product[]>('/api/products', { params });
  },

  getById: (id: number) => api.get<Product>(`/api/products/${id}`),

  create: (data: Partial<Product>) => api.post<Product>('/api/products', data),

  update: (id: number, data: Partial<Product>) =>
    api.put<Product>(`/api/products/${id}`, data),

  delete: (id: number) => api.delete(`/api/products/${id}`),

  getLowStock: () => api.get<Product[]>('/api/admin/products/low-stock'),

  updateStock: (id: number, quantity: number) =>
    api.put<Product>(`/api/admin/inventory/${id}`, null, { params: { quantity } }),
};

// ─── Orders ───────────────────────────────────────────────────────────────

export const orderService = {
  placeOrder: (items: { productId: number; quantity: number }[]) =>
    api.post<Order>('/api/orders', { items }),

  getMyOrders: () => api.get<Order[]>('/api/orders'),

  getById: (id: number) => api.get<Order>(`/api/orders/${id}`),

  cancelOrder: (id: number) => api.delete<Order>(`/api/orders/${id}`),

  // Admin
  getAllOrders: () => api.get<Order[]>('/api/admin/orders'),

  updateStatus: (id: number, status: string) =>
    api.put<Order>(`/api/admin/orders/${id}/status`, null, { params: { status } }),

  updatePickStatus: (id: number, pickStatus: string) =>
    api.put<Order>(`/api/admin/orders/${id}/pick-status`, null, { params: { pickStatus } }),
};

// ─── Admin ────────────────────────────────────────────────────────────────

export const adminService = {
  getDashboard: () => api.get<DashboardStats>('/api/admin/dashboard'),
};

// ─── Warehouses ───────────────────────────────────────────────────────────

export const warehouseService = {
  getAll: () => api.get<import('../types').Warehouse[]>('/api/warehouses'),
  getById: (id: number) => api.get<import('../types').Warehouse>(`/api/warehouses/${id}`),
  create: (data: Partial<import('../types').Warehouse>) => api.post<import('../types').Warehouse>('/api/warehouses', data),
  getInventory: (warehouseId: number) => api.get<import('../types').WarehouseInventory[]>(`/api/warehouses/${warehouseId}/inventory`),
  updateInventory: (data: { warehouseId: number; productId: number; quantity: number; reorderLevel?: number; rackBinLocation?: string }) =>
    api.put<import('../types').WarehouseInventory>('/api/warehouses/inventory', data),
};

// ─── Stock Transfers ──────────────────────────────────────────────────────

export const transferService = {
  getAll: () => api.get<import('../types').StockTransfer[]>('/api/transfers'),
  getById: (id: number) => api.get<import('../types').StockTransfer>(`/api/transfers/${id}`),
  request: (data: { sourceWarehouseId: number; destinationWarehouseId: number; productId: number; quantity: number; notes?: string }) =>
    api.post<import('../types').StockTransfer>('/api/transfers', data),
  approve: (id: number) => api.put<import('../types').StockTransfer>(`/api/transfers/${id}/approve`),
  dispatch: (id: number) => api.put<import('../types').StockTransfer>(`/api/transfers/${id}/dispatch`),
  complete: (id: number) => api.put<import('../types').StockTransfer>(`/api/transfers/${id}/complete`),
  cancel: (id: number) => api.put<import('../types').StockTransfer>(`/api/transfers/${id}/cancel`),
};

// ─── Audit Logs ───────────────────────────────────────────────────────────

export const auditLogService = {
  getAll: () => api.get<import('../types').AuditLog[]>('/api/audit-logs'),
  getByEntity: (entityType: string, entityId: number) =>
    api.get<import('../types').AuditLog[]>('/api/audit-logs/entity', { params: { entityType, entityId } }),
};

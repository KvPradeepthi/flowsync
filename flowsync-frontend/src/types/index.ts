// ─── API types matching backend DTOs ──────────────────────────────────────

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  reorderLevel: number;
  category: string;
  warehouseLocation: string;
  imageUrl: string;
  active: boolean;
  lowStock: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  warehouseLocation: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export type OrderStatus =
  | 'PLACED'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

export type PickStatus = 'PENDING_PICK' | 'PICKED' | 'PACKED' | 'SHIPPED';

export interface Order {
  id: number;
  userId: number;
  userName: string;
  totalAmount: number;
  orderStatus: OrderStatus;
  pickStatus: PickStatus;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  name: string;
  role: 'CUSTOMER' | 'ADMIN';
}

export interface DashboardStats {
  totalProducts: number;
  totalOrders: number;
  pendingOrders: number;
  lowStockCount: number;
  cancelledOrders: number;
  deliveredOrders: number;
}

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

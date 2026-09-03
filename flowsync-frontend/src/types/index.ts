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
  role: 'CUSTOMER' | 'ADMIN' | 'WAREHOUSE_MANAGER' | 'SALES';
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

export interface Warehouse {
  id: number;
  code: string;
  name: string;
  location?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface WarehouseInventory {
  id: number;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  productId: number;
  productSku: string;
  productName: string;
  quantity: number;
  reorderLevel: number;
  rackBinLocation?: string;
  updatedAt: string;
}

export type TransferStatus = 'PENDING' | 'APPROVED' | 'IN_TRANSIT' | 'COMPLETED' | 'CANCELLED';

export interface StockTransfer {
  id: number;
  transferNumber: string;
  sourceWarehouseId: number;
  sourceWarehouseCode: string;
  sourceWarehouseName: string;
  destinationWarehouseId: number;
  destinationWarehouseCode: string;
  destinationWarehouseName: string;
  productId: number;
  productSku: string;
  productName: string;
  quantity: number;
  status: TransferStatus;
  requestedByEmail: string;
  approvedByEmail?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuditLog {
  id: number;
  userId?: number;
  userEmail: string;
  action: string;
  entityType: string;
  entityId: number;
  oldValue?: string;
  newValue?: string;
  details?: string;
  timestamp: string;
}

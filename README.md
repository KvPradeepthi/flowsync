# FlowSync
**Integrated Order, Inventory, and Warehouse Management System**

FlowSync is a unified, full-stack B2B/B2C web application designed to handle end-to-end e-commerce and warehouse logistics. It serves as a single source of truth for product catalogs, customer orders, real-time inventory deduction, and warehouse dispatching.

---

## 🏗️ Architecture & Tech Stack

**Backend:**
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.3.x (Spring Data JPA, Spring Security, Spring Validation)
*   **Database:** MySQL 8 (mapped via Hibernate / JPA) + In-memory H2 for zero-friction testing
*   **Security:** Stateless JWT Authentication + Fine-grained Method Security (`@PreAuthorize`)
*   **Concurrency Control:** Pessimistic write locking (`SELECT ... FOR UPDATE`) during checkout; Optimistic locking (`@Version`) for inventory records
*   **Build Tool:** Maven

**Frontend:**
*   **Framework:** React 18 / 19
*   **Language:** TypeScript
*   **Tooling:** Vite
*   **Routing:** React Router v6 / v7
*   **HTTP Client:** Axios (with automated JWT interceptors)
*   **Styling:** Custom CSS Design System (Dark Theme, Responsive)

---

## ✨ Enterprise SCM Features

### 1. Multi-Warehouse Stock Transfers
Logistics facilities often need to rebalance inventory across locations. FlowSync implements an end-to-end multi-warehouse transfer state machine:
```
Warehouse A (Source) ──[Transfer Request]──> Warehouse B (Destination)
        │
        ├── PENDING      (Created by warehouse staff or manager)
        ├── APPROVED     (Authorized by manager or admin)
        ├── IN_TRANSIT   (Stock deducted from source facility)
        ├── COMPLETED    (Stock incremented at destination facility)
        └── CANCELLED    (Automatic rollback & stock restoration)
```

### 2. Immutable Audit Logging
Enterprise governance demands traceability for high-impact operations:
*   Tracks: `userId`, `userEmail`, `action`, `entityType`, `entityId`, `oldValue`, `newValue`, and `timestamp`.
*   Logged operations: Manual stock overrides, order cancellations, stock transfer approvals, and dispatch events.
*   Accessible to administrators via `GET /api/audit-logs`.

### 3. Role-Based Access Control (RBAC)
FlowSync maps real-world business responsibilities to fine-grained endpoint permissions:
*   **`ADMIN`**: Global access to users, products, all warehouses, system settings, and audit trails.
*   **`WAREHOUSE_MANAGER`**: Facility inventory adjustments, stock transfer approvals/dispatches, and warehouse pick/pack fulfillment.
*   **`SALES`**: Customer order tracking, order cancellation assistance, and sales reporting.
*   **`CUSTOMER`**: Product catalog exploration, shopping cart management, and personal order checkout.

### 4. High-Concurrency Transaction Handling & Data Integrity
*   **Atomic Order Placement:** Checkout and stock deduction run inside `@Transactional`.
*   **Pessimistic Locking:** `ProductRepository.findByIdForUpdate()` uses `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`) to eliminate overselling and race conditions when multiple users check out the last remaining stock simultaneously.
*   **Optimistic Locking:** `@Version` annotations prevent lost updates during concurrent administrative inventory adjustments.

---

## 🚀 Getting Started (Local Development)

### Option A: 1-Command Docker Compose (Recommended)
Run MySQL 8, the Spring Boot backend, and React frontend with one command:
```bash
docker compose up --build
```
*   Frontend: `http://localhost:5173`
*   Backend API: `http://localhost:8080`
*   MySQL 8: `localhost:3306` (`flowsync_db`, credentials: `root`/`root`)

### Option B: Local Spring Boot & Vite

#### Prerequisites:
*   Java 21 installed (`JAVA_HOME` configured).
*   Node.js 18+ installed.

#### 1. Run the Backend (Spring Boot)
```bash
cd flowsync-backend

# Windows
.\mvnw.cmd spring-boot:run

# Mac / Linux
./mvnw spring-boot:run
```
*Note: To run the automated test suite against the in-memory H2 database:*
```bash
.\mvnw.cmd test
```

#### 2. Run the Frontend (React)
```bash
cd flowsync-frontend
npm install
npm run dev
```

---

## 🧪 Automated Test Suite
FlowSync features a comprehensive automated testing suite:
*   **Unit Tests (JUnit 5 + Mockito):**
    *   `OrderServiceTest`: Validates atomic stock deduction, insufficient stock rollback, and cancellation inventory recovery.
    *   `StockTransferServiceTest`: Validates the full transfer lifecycle (`PENDING` → `APPROVED` → `IN_TRANSIT` → `COMPLETED` / `CANCELLED`), warehouse isolation, and stock restoration.
*   **Integration Tests (Spring Boot + MockMvc + H2):**
    *   `SecurityRBACTest`: Verifies stateless JWT filters and role enforcement (`ADMIN`, `WAREHOUSE_MANAGER`, `CUSTOMER`) across secured endpoints.

---

## 📦 Deployment Architecture (AWS)
The project includes container specifications (`Dockerfile`, `docker-compose.yml`) and cloud architecture design documentation for AWS ([AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md)):
1. **Amazon RDS (MySQL 8)**: Multi-AZ transactional database.
2. **Amazon EC2**: Containerized Spring Boot service.
3. **Amazon S3 + CloudFront**: Static SPA distribution.

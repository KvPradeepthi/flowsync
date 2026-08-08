# FlowSync
**Integrated Order, Inventory, and Warehouse Management System**

FlowSync is a unified, full-stack B2B/B2C web application designed to handle end-to-end e-commerce and warehouse logistics. It serves as a single source of truth for product catalogs, customer orders, real-time inventory deduction, and warehouse dispatching.

---

## 🏗️ Architecture & Tech Stack

**Backend:**
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.3.x
*   **Database:** MySQL 8 (mapped via Spring Data JPA / Hibernate)
*   **Security:** Spring Security with Stateless JWT Authentication
*   **Build Tool:** Maven

**Frontend:**
*   **Framework:** React 18
*   **Language:** TypeScript
*   **Tooling:** Vite
*   **Routing:** React Router v6
*   **HTTP Client:** Axios (with automated JWT interceptors)
*   **Styling:** Custom CSS Design System (Dark Theme, Responsive)

---

## ✨ Core Features

### For Customers (B2B / B2C)
*   **Authentication:** Secure registration and login yielding JWT access tokens.
*   **Product Catalog:** Browse active products with real-time stock levels and warehouse locations.
*   **Shopping Cart:** Manage quantities with automatic limits based on current inventory.
*   **Order Placement:** One-click checkout that atomically deducts inventory and generates order records.
*   **Order Tracking:** View historical orders and monitor their fulfillment status in real-time.

### For Warehouse / Administrators
*   **Unified Dashboard:** View high-level metrics (total orders, pending fulfillment, low stock alerts).
*   **Inventory Management:** Update physical stock quantities manually when new shipments arrive.
*   **Order Fulfillment Workflow:**
    *   Update `OrderStatus` (PLACED → CONFIRMED → PROCESSING → SHIPPED → DELIVERED).
    *   Update `PickStatus` (PENDING_PICK → PICKED → PACKED → SHIPPED) mapping to warehouse floor operations.
*   **Automated Monitoring:** Background Spring `@Scheduled` tasks run hourly to log and alert on items dipping below their reorder threshold.

---

## 🚀 Getting Started (Local Development)

### Prerequisites
*   Java 21 installed (`JAVA_HOME` configured).
*   Node.js 18+ installed.
*   (Optional) MySQL server installed and running. *Note: The app is currently configured to use an in-memory H2 database by default if MySQL variables are not provided.*

### 1. Run the Backend (Spring Boot)
Open a terminal in the `flowsync-backend` directory:
```bash
# Mac / Linux
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```
The API will start at `http://localhost:8080`.

### 2. Run the Frontend (React)
Open a new terminal in the `flowsync-frontend` directory:
```bash
npm install
npm run dev
```
The React UI will start at `http://localhost:5173`. Open this URL in your browser.

---

## 📦 Deployment (AWS)

The system is designed to be cloud-native and deployable on AWS.
Please refer to the detailed [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) guide included in this repository for step-by-step instructions on setting up:
1. **Amazon RDS** for the MySQL Database.
2. **Amazon EC2** for hosting the Spring Boot backend.
3. **Amazon S3 + CloudFront** for globally distributing the React frontend.

---

## 🛡️ Security Design
*   **Stateless Architecture:** No session state is held on the server. Scaling the Spring Boot backend horizontally requires zero sticky-session configuration.
*   **Transactional Integrity:** Order placement and inventory deduction occur inside a `@Transactional` block. If stock runs out mid-transaction, the entire operation rolls back securely.
*   **Exception Handling:** A global `@RestControllerAdvice` prevents stack traces from leaking to the client, returning sanitized JSON error responses instead.
*   **Password Hashing:** Passwords are never stored in plaintext; they are hashed using BCrypt.

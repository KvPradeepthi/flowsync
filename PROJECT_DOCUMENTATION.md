# FlowSync — Integrated Order, Inventory & Warehouse Management System
### Project Documentation / プロジェクト詳細設計書

---

## 1. Introduction / はじめに

**English:**  
FlowSync is a full-stack business web application built to manage the complete lifecycle of customer orders, product inventory, and multi-facility warehouse fulfillment on a single platform. It connects the customer-facing ordering process with backend inventory control, inter-warehouse stock transfers, and real-time status tracking, ensuring stock levels stay strictly accurate and traceable as orders and logistics operations take place.

**日本語:**  
FlowSync は、顧客注文・商品在庫・拠点倉庫出荷業務のライフサイクル全体を一つのプラットフォームで管理するために開発されたフルスタック業務アプリケーションです。顧客側の注文プロセスと、バックエンドの在庫管理・複数倉庫間での在庫移動・ステータス追跡を連携させることで、注文の発生や物流処理に応じて在庫数量を常に正確かつ追跡可能に保ちます。

---

## 2. Project Overview / プロジェクト概要

**English:**  
The platform allows customers to browse an active product catalog, place orders, and track order status through to delivery. Every order is validated and processed on the server: stock availability is checked, inventory is deducted transactionally, and the order total is calculated from server-side product data rather than client input. 

Administrators and warehouse managers manage the product catalog, monitor inventory across multiple physical facilities, execute inter-warehouse stock transfers, audit operational actions, and move orders through a defined fulfillment and warehouse pick/pack workflow.

**日本語:**  
本プラットフォームでは、顧客が有効な商品カタログを閲覧し、注文を行い、配送までの注文ステータスを追跡できます。すべての注文はサーバー側で検証・処理されます。在庫の有無を確認し、トランザクション処理により在庫を減算し、注文合計はクライアントからの入力ではなくサーバー側の商品データに基づいて算出されます。

管理者および倉庫担当者は、商品カタログの管理、複数拠点にまたがる在庫の監視、倉庫間在庫移動の実行、業務操作の監査ログ確認、および定義された出荷・倉庫ピッキング/梱包ワークフローに沿った注文処理を行います。

---

## 3. Problem Statement / 課題

**English:**  
Businesses handling online orders and physical warehouse inventory often struggle to keep stock counts accurate in real time across multiple facilities, leading to overselling, delayed fulfillment, manual reconciliation errors, and lack of accountability for inventory adjustments. 

FlowSync addresses this by tying order creation and inventory deduction together in a single atomic transaction with database-level locking, providing multi-warehouse stock transfer state management, maintaining an immutable audit log of all system changes, and giving staff a clear, status-driven view of every order from placement to dispatch.

**日本語:**  
オンライン注文と複数拠点の物理在庫を扱う事業者は、リアルタイムでの在庫数の正確な把握に苦労することが多く、売り越しや出荷遅延、注文システムと倉庫現場との手作業による突合ミス、在庫修正履歴の不透明さが発生しがちです。

FlowSync は、注文作成と在庫減算をデータベースレベルのロックを伴う一つの原子的なトランザクションとして結び付け、複数倉庫間の在庫移動ステータス管理、すべての重要操作に対する不変の監査ログ、および注文が受付から出荷までどの段階にあるかを明確に可視化する仕組みを提供することで、これらの課題に対応します。

---

## 4. Objectives / 目的

* **English:** Provide accurate, real-time inventory tracking tied to order processing  
  **日本語:** 注文処理と連動したリアルタイムかつ正確な在庫管理を実現する
* **English:** Ensure transactional integrity and prevent overselling via pessimistic and optimistic locking  
  **日本語:** 悲観的・楽観的ロックにより、同時注文時の売り越しを防止しトランザクションの整合性を確保する
* **English:** Support multi-warehouse logistics with structured stock transfer requests and approvals  
  **日本語:** 承認・出荷・入荷確認を伴う構造化された複数倉庫間在庫移動ワークフローを提供する
* **English:** Guarantee enterprise traceability and compliance through immutable audit logging  
  **日本語:** 不変の監査ログにより、在庫変更や業務操作の追跡可能性とコンプライアンスを担保する
* **English:** Give warehouse staff a clear order and pick/pack status workflow  
  **日本語:** 倉庫スタッフに明確な注文・ピッキング/梱包ステータスのワークフローを提供する
* **English:** Support secure, role-based access control (RBAC) across administrative and operational roles  
  **日本語:** 管理者・倉庫管理者・営業・顧客に応じた安全なロールベースアクセス制御（RBAC）を実現する
* **English:** Design the system to be cloud-deployable, containerized, and horizontally scalable  
  **日本語:** コンテナ化され、クラウド上へのデプロイと水平スケーリングが容易なシステム設計とする

---

## 5. Key Features / 主な機能

* **English:** Fine-grained Role-Based Access Control (RBAC) with `ADMIN`, `WAREHOUSE_MANAGER`, `SALES`, and `CUSTOMER` roles protected at route and method levels (`@PreAuthorize`)  
  **日本語:** `ADMIN`、`WAREHOUSE_MANAGER`、`SALES`、`CUSTOMER` の4つのロールに対応した、エンドポイントおよびメソッド単位（`@PreAuthorize`）でのきめ細やかなロールベースアクセス制御
* **English:** Multi-Warehouse Stock Transfer System with a 5-stage lifecycle (`PENDING` → `APPROVED` → `IN_TRANSIT` → `COMPLETED` / `CANCELLED`) and automated inventory compensation  
  **日本語:** 5段階のステータス遷移（`PENDING` → `APPROVED` → `IN_TRANSIT` → `COMPLETED` / `CANCELLED`）と自動在庫相殺・復元ロジックを備えた複数倉庫間在庫移動機能
* **English:** Immutable Audit Logging tracking actor, action, target entity, previous value, new value, and timestamp for all critical business operations  
  **日本語:** 操作者、アクション、対象エンティティ、変更前の値、変更後の値、タイムスタンプを永続記録する不変の監査ログ機能
* **English:** High-Concurrency Transaction Management utilizing pessimistic write locking (`SELECT ... FOR UPDATE`) to prevent overselling race conditions, alongside optimistic locking (`@Version`) for inventory records  
  **日本語:** 同時注文による売り越しを防ぐ悲観的ロック（`SELECT ... FOR UPDATE`）と、在庫修正の競合を防ぐ楽観的ロック（`@Version`）を組み合わせた高並行性トランザクション管理
* **English:** Product catalog with real-time aggregated stock levels, warehouse locations, and low-stock alerts  
  **日本語:** リアルタイム在庫数、保管倉庫ロケーション、低在庫アラートを備えた商品カタログ
* **English:** Transactional order placement with server-side price calculation and stock validation  
  **日本語:** サーバー側での価格算出と在庫検証を伴う、トランザクション保護された注文処理
* **English:** Order fulfillment lifecycle (`PLACED` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`) and pick status (`PENDING_PICK` → `PICKED` → `PACKED` → `SHIPPED`)  
  **日本語:** 注文ステータスおよび倉庫作業と連動したピッキング/梱包ステータスの進捗管理
* **English:** Admin & Operations dashboard providing real-time metrics on orders, inventory levels, and stock transfers  
  **日本語:** 注文数・在庫水準・在庫移動状況をリアルタイムに把握できる管理・業務ダッシュボード
* **English:** Scheduled background job (`@Scheduled`) to detect and log low-stock items dipping below reorder thresholds  
  **日本語:** 発注点を下回った低在庫商品を自動検知・ログ記録する定期バッチジョブ
* **English:** Centralized exception handling returning sanitized, structured JSON error responses  
  **日本語:** クライアントへ統一された安全なJSONエラーレスポンスを返却する一元化例外ハンドリング

---

## 6. System Architecture / システム構成

**English:**  
The frontend (React 18/19 + TypeScript + Vite) communicates with the Spring Boot backend over REST APIs secured by stateless JWT authentication. 

The backend follows clean layered architecture (Controller → Service → Repository → Entity), enforcing business rules, multi-warehouse state machines, and concurrency locking. **MySQL 8** serves as the primary relational data store in production and local environments, while an in-memory **H2 Database** is utilized for zero-friction automated testing.

**日本語:**  
フロントエンド（React 18/19 + TypeScript + Vite）は、ステートレスな JWT 認証で保護された REST API を介して Spring Boot バックエンドと通信します。

バックエンドは責務が明確なレイヤードアーキテクチャ（Controller → Service → Repository → Entity）に従い、業務ロジック、複数倉庫間ステータス遷移、排他制御を実行します。リレーショナルデータストアには **MySQL 8** を採用し、自動テスト環境では **H2 Database** による高速・自己完結型の検証を実現しています。

```
React (TypeScript + Vite) Frontend
           │
      REST API / JWT
           │
   Spring Boot Backend
 ┌─────────┴─────────┐
 │ Controller Layer  │ (Route & Method Security: @PreAuthorize)
 │ Service Layer     │ (Atomic @Transactional, State Machines, Audit Logging)
 │ Repository Layer  │ (Spring Data JPA, Pessimistic / Optimistic Locking)
 └─────────┬─────────┘
           │
     Database Store
 ┌─────────┴─────────┐
 │ MySQL 8 (Prod/Dev)│ (Docker Compose / Cloud RDS)
 │ H2 (Test Profile) │ (Automated Unit & Integration Test Suite)
 └───────────────────┘
```

---

## 7. Technology Stack / 技術スタック

| Category | Technologies Used | Notes / 備考 |
| :--- | :--- | :--- |
| **Frontend** | React 18/19, TypeScript, Vite, React Router, Axios | Custom responsive CSS design system, JWT interceptors |
| **Backend** | Java 21, Spring Boot 3.3.x, Spring Data JPA, Spring Security | Layered enterprise architecture, `@Transactional`, `@PreAuthorize` |
| **Database** | MySQL 8 (via `mysql-connector-j` & Hibernate JPA) | Full relational integrity, foreign key constraints, indexes |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, MockMvc, H2 | 18 automated unit and integration tests (0 failures) |
| **Concurrency & Locks** | Pessimistic Write Locking (`PESSIMISTIC_WRITE`), Optimistic Locking (`@Version`) | Eliminates overselling anomalies and lost update races |
| **Authentication & RBAC** | Stateless JWT (jjwt 0.12.x), BCrypt password hashing | 4 roles: `ADMIN`, `WAREHOUSE_MANAGER`, `SALES`, `CUSTOMER` |
| **Containerization** | Docker, Docker Compose | Multi-container setup (MySQL 8 + Backend + Frontend) |
| **Cloud Architecture (Designed)** | AWS RDS (MySQL 8), AWS EC2 (Backend), AWS S3 + CloudFront (Frontend) | Designed & documented cloud deployment topology |
| **Tools & CI/CD** | Git, GitHub, Maven, npm, Postman | Version control, dependency management, API testing |

---

## 8. Current Status & Roadmap / 現在の状況と今後の計画

**English:**  
The complete core backend and frontend architecture is fully implemented, containerized, and tested:
- **Implemented & Verified:**
  - Multi-warehouse stock transfer state machine with automatic compensation
  - Immutable audit logging across all critical business mutations
  - 4-tier Role-Based Access Control (`ADMIN`, `WAREHOUSE_MANAGER`, `SALES`, `CUSTOMER`)
  - Concurrency locking preventing checkout race conditions
  - Database standardized on MySQL 8 with 1-command Docker Compose orchestration
  - 18 automated tests passing cleanly (unit tests for orders & transfers, integration tests for security)
- **Next Phase Roadmap:**
  - Deployment onto AWS infrastructure (RDS MySQL, EC2 backend, S3/CloudFront frontend) following the documented runbook
  - Further UI extensions for warehouse floor barcode scanning and batch dispatching

**日本語:**  
バックエンドおよびフロントエンドの中核アーキテクチャはすべて実装され、コンテナ化および自動テストによる検証が完了しています：
- **実装・検証完了:**
  - 自動相殺ロジックを備えた複数倉庫間在庫移動ステータスマシン
  - すべての重要操作を追跡する不変の監査ログ機能
  - 4段階のロールベースアクセス制御（`ADMIN`、`WAREHOUSE_MANAGER`、`SALES`、`CUSTOMER`）
  - 同時注文時の競合を防ぐ排他制御（悲観的・楽観的ロック）
  - MySQL 8 への統一と、1コマンドで起動可能な Docker Compose 環境の提供
  - 18件の自動テスト全件合格（注文・在庫移動の単体テスト、セキュリティ統合テスト）
- **今後のロードマップ:**
  - 設計文書に沿った AWS クラウドインフラ（RDS MySQL、EC2、S3/CloudFront）への本番デプロイ実施
  - 倉庫現場向けバーコード読み取りや一括出荷 UI の拡充

---

## 9. Conclusion / 結論

**English:**  
FlowSync demonstrates a rigorous, enterprise-grade application of modern full-stack development. By integrating Java 21 / Spring Boot 3 backend engineering, MySQL relational data modeling, atomic transaction boundaries, pessimistic locking, multi-warehouse SCM workflows, immutable audit logging, and comprehensive automated test coverage with a responsive React/TypeScript user interface, FlowSync solves real-world logistics challenges with engineering precision.

**日本語:**  
FlowSync は、現代のフルスタック開発をエンタープライズ水準の実践的アプローチで体現したプロジェクトです。Java 21 / Spring Boot 3 によるバックエンド開発、MySQL による堅牢なデータモデリング、原子的トランザクション管理、悲観的ロックによる排他制御、複数倉庫間 SCM ワークフロー、不変の監査ログ、および網羅的な自動テストを、操作性に優れた React/TypeScript フロントエンドと統合することで、現実の物流・在庫管理の課題をエンジニアリングの観点から高精度に解決しています。

* **GitHub Repository:** [https://github.com/KvPradeepthi/flowsync](https://github.com/KvPradeepthi/flowsync)

---

## 10. Developer Information / 開発者情報

**Veera Pradeepthi Kamichetty**  
Full-Stack Developer | Java / Spring Boot | React / TypeScript | MySQL | B.Tech, Aditya University

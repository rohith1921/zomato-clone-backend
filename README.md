# 🍔 Zomato Clone Backend (High-Scale Order Engine)

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)

A robust, production-grade **Modular Monolith** designed to handle high-concurrency food ordering scenarios. This system implements **Pessimistic Locking** for inventory safety during flash sales and **Redis-based Idempotency** to prevent double-spending during payment processing.

---

## 🏗️ Architecture & Design

The system follows **Domain-Driven Design (DDD)** principles, structured as a Modular Monolith to ensure boundary isolation while maintaining the simplicity of deployment.

| Module | Responsibility | Key Tech |
| :--- | :--- | :--- |
| **API Gateway** | Entry point, routing, and error handling. | Spring Web |
| **Catalog** | Manages restaurants, menus, and dynamic pricing. | Redis (L1 Cache) |
| **Order** | Core checkout flow, state machine management. | Postgres, JPA |
| **Inventory** | Stock management with concurrency locks. | **Pessimistic Locking** |
| **Payment** | Transaction ledger and webhook processing. | **Redis `SETNX`** |



---

## 🚀 Key Engineering Challenges Solved

### 1. The "Flash Sale" Concurrency Problem
**Scenario:** A restaurant has **100 burgers**. During a flash sale, **105 users** attempt to buy them at the exact same millisecond.
**Solution:** Implemented **Postgres Row-Level Locking (`SELECT ... FOR UPDATE`)**. This forces the database to serialize requests for the specific inventory row.
**Result:** - **100** transactions succeed.
- **5** transactions fail gracefully.
- **0** Overselling (Data Integrity Maintained).

### 2. Double-Payment Protection (Idempotency)
**Scenario:** A payment gateway (e.g., Razorpay/Stripe) experiences network latency and sends the "Payment Success" webhook **twice** for the same order.
**Solution:** utilized **Redis Atomic Operations (`SETNX`)**.
- The system sets a key `payment:processed:{txnId}` with a 24-hour TTL.
- If the key exists, the request is rejected immediately at the cache layer, protecting the database from duplicate writes.

### 3. Dynamic Surge Pricing
**Scenario:** Prices need to change instantly based on demand without redeploying the application.
**Solution:** Prices are fetched from Redis. An Admin API updates the Redis key, reflecting the new price immediately for all users.

---

## 🛠️ Tech Stack

* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 3.2
* **Database:** PostgreSQL 15 (Dockerized)
* **Caching:** Redis 7 (Dockerized) - Used for Session, Pricing & Idempotency
* **ORM:** Hibernate / Spring Data JPA
* **Containerization:** Docker & Docker Compose
* **Build Tool:** Maven

---

## ⚡ How to Run Locally

The entire system (App + DB + Redis) is containerized. You do not need to install Java or Postgres locally.

### Prerequisites
* Docker Desktop installed and running.

### Steps
1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/rohith1921/zomato-clone-backend.git](https://github.com/rohith1921/zomato-clone-backend.git)
    cd zomato-clone-backend
    ```

2.  **Start the Infrastructure**
    This command builds the JAR, creates the images, and networking.
    ```bash
    docker-compose up -d --build
    ```

3.  **Verify Deployment**
    ```bash
    docker ps
    ```
    *Ensure `food_ordering_app`, `food_db_final`, and `food_redis_final` are healthy.*

    
---


## 🔌 API Documentation

### 1. Catalog Service
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/menu/{restaurantId}` | Fetch the menu (Cached in Redis). |
| `POST` | `/api/v1/admin/surge` | **Admin Only.** Set a surge multiplier for an item. |

**Example: Fetch Menu**
```bash
curl http://localhost:8080/api/v1/menu/rest-1
```

**Example: Enable Surge Pricing (1.5x Multiplier)**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/surge?itemId=ITEM_UUID_HERE&multiplier=1.5"
```
*(After running this, fetching the menu will show the updated price immediately).*

### 2. Order Service
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/orders` | Creates an order, locks inventory, and returns Order ID. |

**Example: Place Order**
```bash
curl -X POST http://localhost:8080/api/v1/orders \
-H "Content-Type: application/json" \
-d '{
    "userId": "user-uuid-1",
    "restaurantId": "rest-1",
    "items": {
        "ITEM_UUID_HERE": 1
    }
}'
```

### 3. Payment Service (Webhook)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/payment/webhook` | Simulates a callback from Razorpay/Stripe. Idempotent. |

**Example: Simulate Payment**
```bash
curl -X POST "http://localhost:8080/api/v1/payment/webhook?amount=150.00&txnId=txn_123&orderId=ORDER_UUID_HERE"
```

---

## 📊 Load Test Results

To validate the concurrency logic, a Python script simulated **105 concurrent users** attacking the system for **100 items**.

**Test Command:** `python load_test.py`

| Metric | Outcome |
| :--- | :--- |
| **Total Users** | 105 |
| **Total Stock** | 100 |
| **Orders Placed** | **100** (Exact) |
| **Oversold Items** | **0** (Pass) |
| **Rejected Transactions** | 5 (Expected) |
| **Throughput** | ~28 TPS |

> *The system successfully rejected the excess requests using Database Locks, preventing negative inventory.*

---

## 📂 Project Structure

```bash
food-ordering-system
├── api-gateway/       # Application Entry Point & Dockerfile
├── catalog/           # Menu & Pricing Logic
├── infra/             # Shared Utilities & Base Entities
├── inventory/         # Stock Management (Locking Logic)
├── order/             # Order Lifecycle Management
├── payment/           # Payment Gateway Integration
├── docker-compose.yml # Infrastructure definition
└── load_test.py       # Python Stress Test Script

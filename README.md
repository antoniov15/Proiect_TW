# Finance Assistant

A **microservices-based personal finance management platform** built with Spring Boot and Spring Cloud. The application enables users to track transactions, manage their accounts, and receive AI-powered financial insights through an integrated chat system.

---

## 👥 Team

| Name | Role |
|---|---|
| Vicas Antonio | Developer |
| Tisca Laurentiu | Developer |
| Ignat Dan | Developer |

---

## 🚀 Key Features

- **Account Management** – Register, authenticate, update, and manage user accounts with role-based access control (USER, VIP, ADMIN)
- **Transaction Tracking** – Full CRUD for financial transactions with filtering, sorting, and categorization by type (INCOME / EXPENSE)
- **AI-Powered Analysis** – Integrated OpenAI GPT model for automatic transaction categorization, financial analysis, and smart transaction creation
- **Conversational Finance Chat** – Persistent chat sessions with an AI financial advisor; full message history stored per user
- **Budget Monitoring** – Stored procedure-based budget status checks (WITHIN_BUDGET / WARNING / EXCEEDED) and monthly expense summaries
- **Admin Dashboard** – Global transaction reports, user analytics, and GDPR-compliant data anonymization
- **Distributed Tracing** – End-to-end request tracing with Zipkin for debugging and performance monitoring
- **API Documentation** – Swagger/OpenAPI docs available on each service

---

## 🏗️ Architecture

```
Client
  │
  ▼
┌─────────────────────┐
│   API Gateway        │  :8072 (secured) / :8073 (open/dev)
│   Spring Cloud GW    │  OAuth2 (Google) + JWT validation
└──────────┬──────────┘
           │ routes via Eureka
           ▼
┌──────────────────────────────────────────────────┐
│              Netflix Eureka Server  :8070          │
└──────┬────────────────┬──────────────────┬────────┘
       │                │                  │
       ▼                ▼                  ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Account     │ │ Transactions │ │     AI       │
│  Service     │ │  Service     │ │  Service     │
│  :8081       │ │  :8082       │ │  :8080       │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       ▼                ▼                ▼
  accountsdb       transactionsdb      aidb
  PostgreSQL       PostgreSQL          PostgreSQL
  :5432            :5433               :5434

              Zipkin Tracing :9411
```

Inter-service communication is handled via **Spring Cloud OpenFeign** (e.g., the Transaction service calls the Account service to validate users).

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Microservices | Spring Cloud 2024.0.1 |
| API Gateway | Spring Cloud Gateway |
| Service Discovery | Netflix Eureka |
| AI Integration | Spring AI 1.0.0 + OpenAI API (GPT-4o-mini) |
| Authentication | OAuth2 Resource Server (JWT) + Google OAuth2 |
| Database | PostgreSQL 15 (3 isolated schemas) |
| ORM | Spring Data JPA / Hibernate |
| Inter-service calls | Spring Cloud OpenFeign |
| Tracing | Micrometer + Brave + Zipkin |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Code generation | Lombok, MapStruct |
| Containerization | Docker, Docker Compose |
| Build | Apache Maven |

---

## 📡 API Overview

All requests go through the API Gateway at `http://localhost:8072` (secured) or `http://localhost:8073` (open/dev).

### Account Service – `/api/v1/accounts`

| Method | Path | Description | Auth |
|---|---|---|---|
| `POST` | `/` | Create a new account | Public |
| `POST` | `/login` | Login and receive JWT | Public |
| `POST` | `/reset-password` | Reset password | Public |
| `GET` | `/{id}` | Get account details | Owner / Admin |
| `PUT` | `/{id}` | Update account | Owner / Admin |
| `DELETE` | `/{id}` | Delete account | Owner / Admin |
| `GET` | `/` | List all accounts | Admin |
| `GET` | `/email/{email}` | Find by email | Admin |
| `GET` | `/search?username=X` | Search by username | Admin |
| `PUT` | `/{id}/promote-vip` | Promote user to VIP | Admin |
| `GET` | `/{id}/summary` | Account summary | Owner / Admin |

### Transaction Service – `/api/transactions`

| Method | Path | Description | Auth |
|---|---|---|---|
| `POST` | `/` | Create a transaction | User |
| `POST` | `/smart` | Create transaction with AI type detection | User |
| `GET` | `/{id}` | Get transaction | Owner / Admin |
| `PUT` | `/{id}` | Update transaction | Owner / Admin |
| `DELETE` | `/{id}` | Delete transaction | Owner / Admin |
| `GET` | `/user/{userId}` | Get all user transactions | Owner / Admin |
| `GET` | `/filter?type=X` | Filter by type (INCOME/EXPENSE) | User |
| `GET` | `/sort?sortBy=X` | Sort transactions | User |
| `GET` | `/admin/global-summary` | Global admin report | Admin |

### AI Service – `/api/ai` & `/api/chats`

| Method | Path | Description | Auth |
|---|---|---|---|
| `POST` | `/api/ai/categorize` | Categorize a transaction description | User |
| `POST` | `/api/ai/analyze-transactions` | AI analysis of user's transactions | User |
| `GET` | `/api/ai/ask?prompt=X` | Ask the AI a free-form question | User |
| `POST` | `/api/ai/create-smart-transaction` | Create transaction with AI type detection | User |
| `POST` | `/api/chats` | Create a new chat session | User |
| `GET` | `/api/chats` | List all chat sessions | User |
| `GET` | `/api/chats/{id}` | Get a specific chat | User |
| `PUT` | `/api/chats/{id}` | Update a chat | User |
| `DELETE` | `/api/chats/{id}` | Delete a chat | User |
| `POST` | `/api/chats/{chatId}/messages` | Add a message to a chat | User |
| `GET` | `/api/chats/{chatId}/messages` | Get all messages in a chat | User |

---

## 🗄️ Database

Three isolated **PostgreSQL** schemas share one database instance per service:

- **`account_schema`** – Users, roles, credentials
- **`transaction_schema`** – Financial transactions, categories, amounts
- **`ai_schema`** – Chat sessions, messages, AI conversation history

### Notable Stored Procedures

| Schema | Function | Purpose |
|---|---|---|
| account | `count_new_users(start, end)` | User growth analytics |
| account | `anonymize_user_data(user_id)` | GDPR compliance |
| account | `check_account_availability(email, username)` | Duplicate detection |
| transaction | `calculate_monthly_expense(user_id, month, year)` | Monthly spending report |
| transaction | `check_budget_status(user_id, limit)` | Budget alerts |
| transaction | `archive_old_transactions(cutoff_date)` | Data retention |
| ai | `get_chat_history(chat_id)` | Retrieve conversation |
| ai | `search_chats_by_content(text)` | Full-text search |
| ai | `get_daily_message_stats(start, end)` | Activity dashboard |
| ai | `anonymize_chat_data(chat_id)` | GDPR compliance |

---

## ⚙️ Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & Docker Compose
- [Java 21](https://adoptium.net/) (for local development)
- [Maven](https://maven.apache.org/) (or use the included `mvnw` wrapper)
- An [OpenAI API key](https://platform.openai.com/) for the AI service
- Google OAuth2 credentials (Client ID & Secret) for the secured gateway

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/antoniov15/Proiect_TW.git
   cd Proiect_TW
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and fill in the required values:
   ```env
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=your_secure_password_here
   POSTGRES_DB=finance_app_db

   AI_API_KEY=your_openai_api_key_here
   AI_MODEL=gpt-4o-mini

   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   ```
   > **Note:** `.env` is listed in `.gitignore` and will never be committed. For production deployments, use a secrets management solution (e.g., AWS Secrets Manager, HashiCorp Vault, or Docker Secrets) instead of a plain `.env` file.

3. **Start all services**
   ```bash
   docker compose up --build
   ```

4. **Verify services are running**

   | Service | URL |
   |---|---|
   | Eureka Dashboard | http://localhost:8070 |
   | API Gateway (secured) | http://localhost:8072 |
   | API Gateway (open/dev) | http://localhost:8073 |
   | Zipkin Tracing UI | http://localhost:9411 |
   | Account Service Swagger | http://localhost:8081/swagger-ui.html |
   | Transaction Service Swagger | http://localhost:8082/swagger-ui.html |
   | AI Service Swagger | http://localhost:8080/swagger-ui.html |

### Development (Hot Reload)

Use the `dev` profile to enable hot reload for the AI and Gateway services:

```bash
docker compose --profile dev up --build
```

This mounts the source directories and uses Spring Boot DevTools for live reloading.

### Running Without Docker

Build and run each service individually with Maven:

```bash
# From the repository root
mvn clean install

# Start each service
cd eurekaserver && mvn spring-boot:run
cd microservice-account && mvn spring-boot:run
cd microservice-transactions && mvn spring-boot:run
cd microservice-ai && mvn spring-boot:run
cd gatewayserver && mvn spring-boot:run
```

---

## 🔐 Security

- **Authentication:** Google OAuth2 login via the secured gateway; issues JWT tokens for downstream services
- **Authorization:** JWT validated on each microservice using Spring Security OAuth2 Resource Server
- **Role-based access:** `ADMIN`, `USER`, `VIP`, `INACTIVE` — enforced with `@PreAuthorize` annotations
- **Data isolation:** Users can only access their own data; admins have full access
- **GDPR:** Data anonymization stored procedures available for both user accounts and chat history

---

## 📁 Project Structure

```
Proiect_TW/
├── eurekaserver/            # Netflix Eureka service registry
├── gatewayserver/           # Spring Cloud Gateway (routing + OAuth2)
├── microservice-account/    # User account management
├── microservice-transactions/ # Financial transaction management
├── microservice-ai/         # AI chat & transaction analysis
├── db_setup.sql             # Database schema initialization
├── account_stored_procedures.sql
├── ai_stored_procedures.sql
├── docker-compose.yml       # Full stack orchestration
├── common-config.yml        # Shared Docker Compose base config
└── .env.example             # Environment variable template
```

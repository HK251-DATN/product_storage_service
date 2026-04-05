# GEMINI.md - Product Storage Service

## Project Overview

The **Product Storage Service** is a high-performance Spring Boot microservice designed to manage warehouse operations, inventory tracking, and bulk product processing for an e-commerce ecosystem. It handles the full lifecycle of product storage—from receiving large wholesale shipments (batches) to splitting them into individual sellable units.

### Key Technologies
- **Runtime**: Java 25
- **Framework**: Spring Boot 4.0.3 (WebMVC, Data JPA)
- **Database**: PostgreSQL (Persistence), Hibernate (ORM)
- **Messaging**: Spring Kafka (Event-driven architecture)
- **Tooling**: Maven, Lombok, `pg_dump` for database management

### Core Architecture
The service follows a standard layered architecture:
- **Controllers**: REST endpoints for warehouse, storage tools, batches, and product details.
- **Services**: Business logic, including complex unit conversions and batch splitting.
- **Repositories**: Spring Data JPA for data access.
- **DAO/Entities**: Persistent models (Warehouse, Rack, Fridge, ProductBatch, ProductDetail, etc.).
- **Messaging**: Kafka consumers for category/product updates and producers for inventory events.

---

## Domain Model & Hierarchy

The system uses a hierarchical model for both storage and products:

### Storage Hierarchy
1.  **Warehouse**: Top-level facility containing multiple storage tools.
2.  **Storage Tool**: Categorized as `RACK` (dry storage) or `FRIDGE` (climate-controlled).
3.  **Rack/Fridge**: Specific containers with capacity limits and environmental status.
4.  **Rack Level**: Individual shelves within a rack with specific weight capacities.

### Product Hierarchy
1.  **SubSubcategory**: The leaf-level category shared across services.
2.  **ProductGeneral**: The product "blueprint" (e.g., Organic Rice 500g).
3.  **ProductBatch**: A bulk shipment received (e.g., 100kg of Rice).
4.  **ProductDetail**: Individual sellable items created from a batch.

---

## Building and Running

### Commands
- **Build**: `./mvnw clean install`
- **Run**: `./mvnw spring-boot:run`
- **Test**: `./mvnw test`
- **Skip Tests**: `./mvnw clean install -DskipTests`

### Environment Configuration
Required environment variables (defined in `.env`):
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL connection.
- `KAFKA_HOST`, `KAFKA_PORT`: Kafka broker connection.
- `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY`: Cloudflare R2 for asset storage.

### Server Configuration
- **Port**: `9200`
- **Standard Response**: All APIs return an `ApiResponse<T>` object containing a `type` (`GOOD`, `ERROR`, `WARN`, `SKIP_AS_GOOD`), `code`, `message`, `detail`, and `timestamp`.

---

## Development Conventions

### Service Layer Patterns
- **Constructor Injection**: Services use `@AllArgsConstructor` for dependency injection.
- **Error Handling**: Throws custom exceptions (e.g., `ProductDetailNotFoundException`) which are handled by the framework.
- **Transactional**: Critical operations like `processProductBatch` are marked `@Transactional`.
- **Pagination**: API pagination starts at `pageNum=1` (1-based index), but service logic converts this to 0-based for `PageRequest`.

### Batch Processing Logic (`ProductDetailService.processProductBatch`)
1.  Validates that the `ProductBatch`'s `processStatus` is `PENDING`.
2.  Checks if the `ProductBatch` is expired (current time > `expiredAt`). If expired, sets status to `EXPIRED` and throws `ProductBatchExpiredException`.
3.  Validates that the `ProductBatch` and `ProductGeneral` share the same `subSubcategoryId`.
4.  Uses `UnitConverter` to calculate split quantity (e.g., 10kg batch / 500g unit = 20 products).
5.  Automates creation of `ProductDetail` records.
6.  Updates `ProductBatch`'s `processStatus` to `PROCESSED`.
7.  Publishes a `BatchDetailCreateEvent` to Kafka for the e-commerce service to update its catalog.

### Entity Management
- **Timestamps**: `created_at` and `updated_at` are automatically managed via `@PrePersist` and `@PreUpdate` callbacks.
- **Lombok**: Extensively used for boilerplate reduction (`@Getter`, `@Setter`, `@Builder`).

---

## Key Files
- `pom.xml`: Project dependencies and Java 25 configuration.
- `src/main/resources/application.yaml`: Centralized configuration.
- `CLAUDE.md`: Detailed developer guidance and architecture map.
- `API_GUIDE_FOR_UI.md`: Comprehensive guide for frontend integration.
- `ProductDetailServiceImpl.java`: Contains the core batch processing business logic.

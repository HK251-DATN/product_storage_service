# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Product Storage Service - A Spring Boot microservice that manages warehouse storage, product inventory, and batch processing for an e-commerce platform. The service integrates with Kafka for event-driven communication with other microservices.

**Stack**: Spring Boot 4.0.3, Java 25, PostgreSQL, Spring Kafka, JPA/Hibernate, Lombok

**Server Port**: 9200

## Build and Run Commands

### Build and Test
```bash
# Build the project
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ProductstorageApplicationTests
```

### Run Application
```bash
# Run the application
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker Build and Run
```bash
# Build Docker image
docker build -t product-storage-service:latest .

# Run container (requires .env file)
docker run -p 9200:9200 --env-file .env product-storage-service:latest
```

### Database Operations
```bash
# Backup database
pg_dump --no-owner --no-privileges --format=plain -U postgres -h localhost -p 5432 product_storage_db > backup.sql

# Restore database
psql -U postgres -h localhost -p 5432 -d product_storage_db < backup.sql
```

## Environment Configuration

Required environment variables (see `.env.example`):
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD` - PostgreSQL connection
- `KAFKA_HOST`, `KAFKA_PORT` - Kafka broker connection
- `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY` - Cloudflare R2 credentials
- `USER_AVATAR_PUBLIC_BUCKET_URL` - Public bucket URL

Configuration is loaded from `.env` file via `application.yaml`.

## Architecture

### Package Structure
```
edu.hcmut.datn.productstorage/
├── common/enums/          # Shared enums (Unit, ProductStatus, StorageType, etc.)
├── config/                # Spring configuration (WebConfig, CORS)
├── controller/            # REST API endpoints
├── dao/                   # JPA entities (database models)
├── dto/                   # Data Transfer Objects
│   ├── request/           # Request DTOs
│   └── response/          # Response DTOs (ApiResponse)
├── exception/             # Custom exceptions (*NotFoundException, *AlreadyExistsException)
├── messaging/             # Kafka integration
│   ├── batchdetail/       # Batch detail producer/events
│   ├── orderitem/         # Order item consumer/events
│   ├── productgeneral/    # Product general consumer/events
│   └── subsubcategory/    # Sub-subcategory consumer/events
├── repository/            # Spring Data JPA repositories
├── service/               # Service interfaces
│   └── impl/              # Service implementations
└── util/                  # Utilities (UnitConverter)
```

### Domain Model

**Storage Hierarchy**:
```
Warehouse (address, usage_percentage, num_of_fridge, num_of_rack)
  └─ StorageTool (tool_type: RACK | FRIDGE, status, usage_percentage)
       ├─ Rack (capacity, current_load, usage_percentage)
       │   └─ RackLevel (level_number, max_weight, current_weight)
       └─ Fridge (temperature, humidity)
```

**Product Hierarchy** (3-layer category system):
```
SubSubcategory (name, description, icon_url)
  └─ ProductGeneral (prod_gen_id, name, img_url, unit, unit_quantity)
       └─ ProductBatch (quantity, unit, received_at, expired_at, provider_id)
            └─ ProductDetail (status, price, num_of_star, storage_tool_id, batch_id)
```

**Key Relationships**:
- ProductGeneral and ProductBatch must have matching `sub_subcategory_id` (validated in batch processing)
- ProductDetail references ProductGeneral, ProductBatch, and StorageTool
- StorageTool belongs to Warehouse
- OrderItem tracks product sales and inventory deductions

### Kafka Event-Driven Architecture

**Consumers** (incoming events from other services):
- `product-general-events` - Creates ProductGeneral entities
- `subsubcategory-events` - Creates SubSubcategory entities  
- `order-item-events` - Processes order items

**Producers** (outgoing events to other services):
- `batch-detail-events` - Publishes batch detail creation to e-commerce service

Consumer pattern: `@KafkaListener` on topic → Service method call → Exception handling with logging

### Batch Processing Logic

The `ProductDetailService.processProductBatch()` method implements critical business logic:

1. Validates ProductBatch and ProductGeneral have matching sub-subcategory
2. Uses `UnitConverter.splitBatch()` to calculate how many ProductDetails can be created from a ProductBatch
3. Unit conversion supports MASS (kg, g) and VOLUME (L, mL) with automatic conversion
4. Creates multiple ProductDetail instances based on calculation
5. Publishes `BatchDetailCreateEvent` to Kafka for e-commerce service

**Example**: 10kg batch + 500g package = 20 ProductDetail instances

### Service Layer Patterns

All services follow the standard CRUD interface pattern:
- `create(T entity)` - Create new entity
- `read(Long id)` - Read by ID (throws *NotFoundException if not found)
- `readAll(Integer pageNum, Integer pageSize)` - Paginated read (PageRequest uses 0-based index internally)
- `update(Long id, T entity)` - Partial update (null fields are ignored)
- `delete(Long id)` - Delete entity

Service implementations are in `service.impl.*ServiceImpl` and use constructor injection with `@AllArgsConstructor`.

#### Special Service Methods

**ProductGeneralService:**
- `getSuitableForBatch(Long batchId)` - Returns Product Generals that match a batch's category and have compatible units. Used for filtering product options when processing a batch.

## REST API Structure

All controllers follow a standard REST pattern with base path `/api/{resource}`:

**Standard CRUD Endpoints** (available on all resources):
- `POST /api/{resource}` - Create new entity
- `GET /api/{resource}/{id}` - Read entity by ID
- `GET /api/{resource}?pageNum=1&pageSize=20` - List all (paginated, 1-based index in API, converted to 0-based internally)
- `PUT /api/{resource}/{id}` - Update entity (partial update, null fields ignored)
- `DELETE /api/{resource}/{id}` - Delete entity

**Available Resources**:
- `/api/warehouse` - Warehouse management
- `/api/storage-tool` - Storage tool operations
- `/api/rack` - Rack-specific operations
- `/api/rack-level` - Rack level management
- `/api/fridge` - Fridge-specific operations
- `/api/product-general` - Product general information
- `/api/product-batch` - Product batch operations
- `/api/product-detail` - Product detail management
- `/api/order-items` - Order item tracking

**Special Endpoints**:
- `POST /api/product-detail/process-batch` - Process a ProductBatch to create multiple ProductDetails (critical batch processing logic)

**Response Format**:
All endpoints return `ApiResponse<T>` with structure:
```json
{
  "status": "SUCCESS" | "ERROR" | "SKIP_AS_GOOD",
  "message": "Operation description",
  "data": T | null
}
```

## Development Conventions

### Entity Timestamps
All entities use `@PrePersist` and `@PreUpdate` callbacks to auto-manage `created_at` and `updated_at` timestamps.

### Exception Handling
Custom exceptions follow naming pattern: `{Entity}NotFoundException`, `{Entity}AlreadyExistsException`. Thrown from service layer, not repositories.

### Lombok Usage
Entities use selective `@Getter`/`@Setter` annotations rather than class-level to control field access. Services use `@AllArgsConstructor` for constructor injection.

### CORS Configuration
Frontend origins are configured in `WebConfig.java`. Add new origins there when deploying to new environments.

### Primary Keys
- ProductGeneral uses manually assigned IDs (`@Id` without `@GeneratedValue`)
- Most other entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment

## Testing

Tests are located in `src/test/java`. The project uses Spring Boot Test dependencies for JPA and WebMVC testing.

## Database Schema

Database schema documentation is located in `db_scheme/README.md`. The service uses Hibernate with `ddl-auto: update` to automatically manage schema changes based on JPA entity definitions.

**Database**: `product_storage_db`  
**Connection**: PostgreSQL via JDBC URL configured in `application.yaml`

## Configuration Files

- `application.yaml` - Main Spring Boot configuration (datasource, JPA, server port, Kafka URL)
- `.env` - Environment-specific variables (DB credentials, Kafka host, R2 credentials)
- `.env.example` - Template for required environment variables

The application uses Spring's `config.import: optional:file:.env[.properties]` to load `.env` files automatically.

## Data Seeding

The service includes a `DataSeeder` configuration class that automatically populates the database with sample Vietnamese fresh food data on first startup.

**Seeded Data Includes**:
- 3 warehouses (TP.HCM locations)
- 8 storage tools (4 racks + 4 fridges) with appropriate temperature settings
- 20 rack levels across multiple racks
- 15 sub-subcategories (fruits, vegetables, meat, seafood, dairy)
- 15 product generals (Vietnamese fresh food products)
- 16 product batches with realistic expiry dates for fresh foods

**Data Seeding Behavior**:
- Only runs if `warehouseRepository.count() == 0` (empty database)
- All entities use Vietnamese names and descriptions
- Product batches have status `PENDING` (ready for batch processing)
- Expiry dates are realistic for fresh food categories:
  - Fruits: 5-10 days
  - Vegetables: 3-7 days
  - Fresh meat: 2-3 days
  - Seafood: 1-2 days (fresh), 15-30 days (frozen)
  - Dairy/Eggs: 5-15 days

**To reset and re-seed**:
```bash
# Drop and recreate the database
psql -U postgres -h localhost -p 5432 -c "DROP DATABASE product_storage_db;"
psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE product_storage_db;"

# Restart the application to trigger seeding
./mvnw spring-boot:run
```

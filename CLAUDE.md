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

Optional environment variables (legacy, not currently used):
- `R2_ACCOUNT_ID`, `R2_ACCESS_KEY`, `R2_SECRET_KEY` - Cloudflare R2 credentials (reserved for future file storage)
- `USER_AVATAR_PUBLIC_BUCKET_URL` - Public bucket URL (reserved for future use)

Configuration is loaded from `.env` file via `application.yaml` using `spring.config.import: optional:file:.env[.properties]`.

## Architecture

### Package Structure
```
edu.hcmut.datn.productstorage/
├── common/enums/          # Shared enums (Unit, ProductStatus, ProductBatchProcessStatus, StorageType, etc.)
├── config/                # Spring configuration (WebConfig, CORS, DataSeeder)
├── controller/            # REST API endpoints
├── dao/                   # JPA entities (database models)
├── dto/                   # Data Transfer Objects
│   ├── request/           # Request DTOs
│   └── response/          # Response DTOs (ApiResponse)
├── exception/             # Custom exceptions (*NotFoundException, *AlreadyExistsException, SubSubcategoryMismatchException, etc.)
├── messaging/             # Kafka integration
│   ├── batchdetail/       # Batch detail producer/events
│   ├── orderpackagingprogress/  # Order packaging progress producer/events
│   ├── orderpick/         # Order pick consumer/events
│   ├── productgeneral/    # Product general consumer/events
│   └── subsubcategory/    # Sub-subcategory consumer/events
├── repository/            # Spring Data JPA repositories
│   └── projector/         # Database projections (PickListItem, ProductDetailForPickItem)
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
SubSubcategory (name, description, icon_url, subcategory_id, avg_shelf_days)
  └─ ProductGeneral (prod_gen_id, name, img_url, description, unit, unit_quantity, subsubcategory_id)
       └─ ProductBatch (quantity, unit, note, received_at, expired_at, provider_id, sub_subcategory_id, process_status)
            └─ ProductDetail (status, price, num_of_star, storage_tool_id, batch_id, prod_gen_id)
                 └─ OrderItem (order_item_id, order_id, batch_detail_id, buyer_id, product_detail_id)
```

**Key Relationships**:
- SubSubcategory is created via Kafka events from back-office service, has `avg_shelf_days` for automatic expiry calculation
- ProductGeneral and ProductBatch must have matching `sub_subcategory_id` (validated in batch processing)
- ProductBatch has `process_status` enum: PENDING, PROCESSED, EXPIRED
- ProductDetail references ProductGeneral, ProductBatch, and StorageTool; has `copy()` method for duplication
- OrderItem has 1-to-1 relationship with ProductDetail via `product_detail_id`
- StorageTool belongs to Warehouse

### Enums

**Unit** - Measurement units for products:
- WEIGHT: `KILOGRAM`, `GRAM`
- VOLUME: `LITER`, `MILLILITER`
- COUNT: `PIECE`, `DOZEN`
- CONTAINER: `PACK`, `BOX`, `BOTTLE`

**UnitCategory** - Unit categorization for conversion logic:
- `WEIGHT` - Mass-based units (kg, g)
- `VOLUME` - Volume-based units (L, mL)
- `COUNT` - Count-based units (piece, dozen)

**ProductStatus** - Product detail lifecycle states:
- `STORED` - In warehouse storage
- `EXPIRED` - Past expiry date
- `PICKED` - Selected for order fulfillment
- `IN_TRANSIT` - Being delivered
- `DELIVERED` - Successfully delivered
- `RETURNED` - Returned by customer
- `DISPOSED` - Removed from inventory

**ProductBatchProcessStatus** - Batch processing states:
- `PENDING` - Received, awaiting processing
- `PROCESSED` - Converted to product details
- `EXPIRED` - Past expiry date, cannot process

**StorageType** - Storage tool types:
- `RACK` - Shelf storage
- `FRIDGE` - Refrigerated storage

**StorageToolStatus** - Storage tool operational states:
- `ACTIVE` - Operational and available
- `INACTIVE` - Not in use
- `FULL` - At capacity
- `IN_MAINTAINANCE` - Under maintenance

### Database Entities

**Storage Entities** (warehouse/storage management):
- `Warehouse` - Physical warehouse locations
- `StorageTool` - Abstract storage tool (rack or fridge)
- `Rack` - Rack storage tool with levels
- `RackLevel` - Individual level within a rack
- `Fridge` - Refrigerated storage tool with temperature control

**Product Entities** (product catalog and inventory):
- `SubSubcategory` - Finest category granularity (e.g., "Thịt Gà", "Rau Muống")
- `ProductGeneral` - General product information (e.g., "Gà Ta Nguyên Con")
- `ProductBatch` - Bulk quantity received from suppliers
- `ProductDetail` - Individual sellable product units

**Order Fulfillment Entities**:
- `OrderItem` - Order items linked to product details

**Entity Counts After Seeding**:
- 3 Warehouses
- 8 StorageTools (4 Racks + 4 Fridges)
- 4 Racks with 20 RackLevels
- 4 Fridges
- 55 SubSubcategories
- 105 ProductGenerals
- 35 ProductBatches (PENDING status)

### Kafka Event-Driven Architecture

**Consumers** (incoming events from other services):
- `product-general-events` - Creates ProductGeneral entities (from back-office service)
- `subsubcategory-events` - Creates SubSubcategory entities (from back-office service)
- `order-pick-requested-events` - Creates pick lists for order fulfillment (from ecommerce service)

**Producers** (outgoing events to other services):
- `batch-detail-events` - Publishes batch detail creation to ecommerce service
- `order-packaging-progress-update-events` - Publishes order packaging progress updates to ecommerce service

Consumer pattern: `@KafkaListener` on topic → Service method call → Exception handling with logging

**Event Flow Example**:
1. Back-office service publishes `subsubcategory-events` and `product-general-events`
2. Product storage service consumes and creates local entities
3. Warehouse staff processes ProductBatch → creates ProductDetails
4. Service publishes `batch-detail-events` to ecommerce service
5. Ecommerce service publishes `order-pick-requested-events` when order is confirmed
6. Product storage service creates pick list and updates packaging progress via `order-packaging-progress-update-events`

### Batch Processing Logic

The `ProductDetailService.processProductBatch()` method implements critical business logic:

1. **Validates ProductBatch**:
   - Checks `process_status` is PENDING (throws ProductBatchAlreadyProcessedException if PROCESSED)
   - Validates not expired (throws ProductBatchExpiredException if past expiry date)
   - Ensures ProductGeneral and ProductBatch have matching `sub_subcategory_id` (throws SubSubcategoryMismatchException)
2. **Unit Conversion**: Uses `UnitConverter.splitBatch()` to calculate how many ProductDetails can be created
   - Supports MASS (kg, g) and VOLUME (L, mL) with automatic conversion
   - Example: 10kg batch + 500g package = 20 ProductDetail instances
3. **Creates ProductDetails**: Generates multiple ProductDetail instances with AVAILABLE status
4. **Updates ProductBatch**: Sets `process_status = PROCESSED`
5. **Publishes Event**: Sends `BatchDetailCreateEvent` to Kafka topic `batch-detail-events` for ecommerce service
6. **Auto-calculates Expiry**: Uses `avg_shelf_days` from SubSubcategory to set batch expiry date

**ProductBatchProcessStatus Enum**:
- `PENDING` - Batch received, not yet processed into product details
- `PROCESSED` - Batch successfully converted to product details
- `EXPIRED` - Batch past expiry date, cannot be processed

### Service Layer Patterns

**Available Services**:
- WarehouseService - Warehouse CRUD operations
- StorageToolService - Storage tool CRUD operations
- RackService - Rack-specific CRUD operations
- RackLevelService - Rack level CRUD operations
- FridgeService - Fridge-specific CRUD operations
- ProductGeneralService - Product general CRUD + `getSuitableForBatch()`
- ProductBatchService - Product batch CRUD operations
- ProductDetailService - Product detail CRUD + `processProductBatch()`
- SubSubcategoryService - SubSubcategory CRUD (no REST controller, Kafka-only)
- OrderItemService - Order item tracking
- PickListService - Pick list management (no entity, uses projections)

**Standard CRUD Services** follow this interface pattern:
- `create(T entity)` - Create new entity
- `read(Long id)` - Read by ID (throws *NotFoundException if not found)
- `readAll(Integer pageNum, Integer pageSize)` - Paginated read (PageRequest uses 0-based index internally)
- `update(Long id, T entity)` - Partial update (null fields are ignored)
- `delete(Long id)` - Delete entity

Service implementations are in `service.impl.*ServiceImpl` and use constructor injection with `@AllArgsConstructor`.

#### Special Service Methods

**ProductGeneralService**:
- `getSuitableForBatch(Long batchId)` - Returns Product Generals that match a batch's category and have compatible units. Used for filtering product options when processing a batch.

**ProductDetailService**:
- `processProductBatch(ProcessProductBatchRequest)` - Core batch processing logic (see Batch Processing Logic section)
- Uses `ProductDetail.copy()` method to duplicate product details efficiently

**OrderItemService**:
- Manages OrderItem entities created from Kafka `order-pick-requested-events`
- Tracks 1-to-1 relationship between order items and product details

**SubSubcategoryService**:
- Manages SubSubcategory entities (no REST controller, only created via Kafka events)
- SubSubcategories include `avg_shelf_days` for automatic batch expiry calculation

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
- `/api/pick-list` - Pick list management (no CRUD, specialized endpoints)

**Special Endpoints**:
- `POST /api/product-detail/process-batch` - Process a ProductBatch to create multiple ProductDetails (critical batch processing logic)
- `GET /api/product-detail/quantity/{batchId}` - Get current quantity of available product details for a batch
- `GET /api/pick-list/{orderId}` - Get pick list for an order (returns List<PickListItem>)
- `PUT /api/pick-list/{orderItemId}/link/{productDetailId}` - Link an order item to a specific product detail
- `GET /api/pick-list/product-detail-list/{orderItemId}` - Get available product details for picking an order item

**Note on PickList**:
PickList is not a database entity but a service that uses repository projections (PickListItem, ProductDetailForPickItem) to provide pick list functionality for order fulfillment workflows.

**Response Format**:
All endpoints return `ApiResponse<T>` with structure:
```json
{
  "status": "SUCCESS" | "ERROR" | "SKIP_AS_GOOD",
  "message": "Operation description",
  "data": T | null
}
```

### Specialized Services

**PickListService** - Order fulfillment pick list management (not backed by an entity):
- `createPickList(OrderPickRequestedEvent)` - Creates pick list from Kafka order-pick-requested event
- `getPickList(Long orderId)` - Returns List<PickListItem> projection with order items and available products
- `linkOrderItem(Long orderItemId, Long productDetailId)` - Links order item to specific product detail for picking
- `getProductDetailCurrentQuantity(Long batchId)` - Returns count of available product details for a batch
- `getProductDetailListForPickItem(Long orderItemId)` - Returns List<ProductDetailForPickItem> for order item selection

**Repository Projections** (in `repository.projector` package):
- `PickListItem` - Projection interface for pick list data (order item + product info)
- `ProductDetailForPickItem` - Projection interface for available product details during picking

**BackOfficeServiceClient** - HTTP client for calling back-office service APIs:
- Used for fetching category/subcategory information when needed
- Configured with back-office service base URL

## Development Conventions

### Entity Timestamps
All entities use `@PrePersist` and `@PreUpdate` callbacks to auto-manage `created_at` and `updated_at` timestamps.

### Exception Handling
Custom exceptions follow naming pattern: `{Entity}NotFoundException`, `{Entity}AlreadyExistsException`. 

**Available Exceptions**:
- `*NotFoundException` - Entity not found (Warehouse, StorageTool, Rack, RackLevel, Fridge, ProductGeneral, ProductBatch, ProductDetail)
- `*AlreadyExistsException` - Duplicate entity creation attempt
- `SubSubcategoryMismatchException` - ProductGeneral and ProductBatch have different sub_subcategory_id
- `ProductBatchExpiredException` - Attempting to process an expired batch
- `ProductBatchAlreadyProcessedException` - Attempting to reprocess an already processed batch

All exceptions are thrown from service layer, not repositories.

### Lombok Usage
Entities use selective `@Getter`/`@Setter` annotations rather than class-level to control field access. Services use `@AllArgsConstructor` or `@RequiredArgsConstructor` for constructor injection.

### Entity-Specific Features

**ProductDetail**:
- Has `copy()` method for efficient duplication during batch processing
- Auto-manages `created_at` and `updated_at` timestamps via `@PrePersist` and `@PreUpdate`
- Status enum: `ProductStatus` (AVAILABLE, SOLD, RESERVED, DAMAGED, etc.)

**ProductBatch**:
- Has `process_status` field: PENDING (default), PROCESSED, EXPIRED
- Auto-initializes to PENDING in constructor
- Contains `sub_subcategory_id` for validation against ProductGeneral

**SubSubcategory**:
- Created only via Kafka events (no REST controller)
- Manually assigned ID (not auto-generated)
- Contains `avg_shelf_days` for automatic batch expiry calculation
- References parent `subcategory_id`

**OrderItem**:
- Created from Kafka `order-pick-requested-events`
- Has 1-to-1 relationship with ProductDetail via `product_detail_id`
- Tracks `order_id`, `batch_detail_id`, and `buyer_id`

**ProductGeneral**:
- Manually assigned `prod_gen_id` (not auto-generated) - ID comes from back-office service
- Must have matching `sub_subcategory_id` with ProductBatch for processing

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

The service includes a `DataSeeder` configuration class that automatically populates the database with comprehensive Vietnamese fresh food data on first startup.

**Seeded Data Includes**:
- **3 warehouses** - TP.HCM locations (Quận 7, Thủ Đức, Gò Vấp)
- **8 storage tools** - 4 racks + 4 fridges with appropriate temperature settings
  - Fridges: 0-8°C (vegetables/fruits), -2-5°C (meat/seafood), 1-6°C (dairy/eggs)
- **20 rack levels** - distributed across 4 racks (4-6 levels each)
- **55 sub-subcategories** - comprehensive Vietnamese fresh food categories:
  - Poultry: Thịt Gà, Thịt Vịt, Thịt Ngan, Thịt Chim Cút, Lòng Gia Cầm
  - Red Meat: Thịt Bò, Thịt Heo, Thịt Dê, Thịt Cừu, Xúc Xích Tươi
  - Seafood: Tôm Tươi, Cá Tươi, Mực Tươi, Cua Ghẹ, Nghêu Sò
  - Leafy Greens: Rau Muống, Cải Xanh, Xà Lách, Rau Dền, Cải Thìa
  - Root Vegetables: Cà Rốt, Khoai Tây, Củ Cải, Bắp, Su Su
  - Fruits: Xoài, Chuối, Dưa Hấu, Ổi, Thanh Long
  - Herbs/Spices: Hành Lá, Tỏi, Gừng, Ớt, Sả
  - Dairy: Fresh Milk varieties, Butter, Cheese, Whipping Cream, Yogurt, Condensed Milk
  - Eggs: Industrial Chicken Eggs, Native Chicken Eggs, Duck Eggs, Quail Eggs, Balut
  - Dairy Alternatives: Soy Milk, Nut Milk, Drinkable Yogurt, Kefir, Aloe Vera Yogurt
- **105 product generals** - 3 variations per sub-subcategory with Vietnamese names and descriptions
- **35 product batches** - realistic quantities (20-200kg) with `PENDING` status, ready for processing

**Data Seeding Behavior**:
- Only runs if `warehouseRepository.count() == 0` (empty database)
- All entities use Vietnamese names and descriptions
- Product batches have `process_status = PENDING` (ready for batch processing)
- **Automatic expiry calculation** using `avg_shelf_days` from SubSubcategory:
  - Shellfish (Nghêu Sò): 1 day
  - Seafood/Poultry Offal: 2 days
  - Poultry/Pork/Herbs: 3 days
  - Beef/Lamb: 4 days
  - Vegetables: 1-3 days
  - Fruits: 5-7 days
  - Root vegetables: 7-30 days
  - Dairy products: 7-180 days
  - Eggs: 7-21 days
- SubSubcategories include `avg_shelf_days` field for automatic expiry date calculation

**To reset and re-seed**:
```bash
# Drop and recreate the database
psql -U postgres -h localhost -p 5432 -c "DROP DATABASE product_storage_db;"
psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE product_storage_db;"

# Restart the application to trigger seeding
./mvnw spring-boot:run
```

**Seeder Summary Log Output**:
```
Database seeding completed successfully!
Summary:
  - 3 warehouses with storage capacity
  - 8 storage tools (4 racks + 4 fridges)
  - 55 product categories ready for inventory
  - 105 product types available
  - 35 fresh batches ready to process
```

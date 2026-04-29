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

**Product Hierarchy** (3-layer category system with provider verification tracking):
```
SubSubcategory (name, description, icon_url, subcategory_id, avg_shelf_days)
  └─ ProductGeneral (prod_gen_id, name, img_url, description, unit, unit_quantity, subsubcategory_id)
       ├─ ProductBatch (CERTIFICATE verified) - Uniquely owned by single certified provider
       │    └─ ProductDetail (with provider attribution on ecommerce)
       │
       └─ ProductBatch (VIDEO verified) - Shared/pooled batch from multiple providers
            └─ ProductSubBatch (individual provider deliveries, pooled together)
                 └─ ProductDetail (without provider attribution on ecommerce)
                      └─ OrderItem (order_item_id, order_id, batch_detail_id, buyer_id, product_detail_id)
```

**Business Logic - Provider Verification & Product Traceability**:

The system implements **two distinct workflows** based on provider verification type, affecting product traceability and ecommerce display:

**CERTIFICATE-Verified Providers** (e.g., VietGAP, GlobalGAP):
- **Purpose**: Premium products with full provider attribution for marketing
- **Example**: Provider A (VietGAP certified) delivers 20kg "Thịt Heo" on 2026-04-29
- **Data Structure**: ProductBatch (batch-of-A, 20kg) → 20 ProductDetails (1kg packs)
- **Ecommerce Display**: "Thịt Heo 1kg - From Provider A (VietGAP Certified)"
- **No ProductSubBatch needed** - batch is uniquely owned by one certified provider

**VIDEO-Verified Providers** (no certificates):
- **Purpose**: Pooled products from multiple providers, no individual attribution
- **Example**: Provider B (20kg) + Provider C (10kg) both deliver "Thịt Heo" on 2026-04-29
- **Data Structure**: 
  - ProductBatch (batch-of-pig-1-20260429, VIDEO verified)
    - ProductSubBatch (Provider B, 20kg) → 20 ProductDetails
    - ProductSubBatch (Provider C, 10kg) → 10 ProductDetails
  - Total: 30 ProductDetails all linked to shared batch
- **Ecommerce Display**: "Thịt Heo 1kg" (no provider name - generic pooled product)
- **ProductSubBatch required** - tracks individual provider contributions to shared batch

**Key Relationships**:
- SubSubcategory is created via Kafka events from back-office service, has `avg_shelf_days` for automatic expiry calculation
- ProductGeneral and ProductBatch must have matching `sub_subcategory_id` (validated in batch processing)
- **ProductBatch verification_type determines batch structure**:
  - `CERTIFICATE`: Single provider batch, no sub-batches, direct to ProductDetail (provider attribution shown)
  - `VIDEO`: Shared/pooled batch, contains multiple ProductSubBatch from different providers (no provider attribution)
- **ProductBatch with VIDEO verification can contain multiple ProductSubBatch entities** (1-to-N relationship via `product_batch_id`)
- ProductBatch has `process_status` enum: WAIT_FOR_DELIVERY, PENDING, PROCESSED, EXPIRED, REJECTED
- ProductSubBatch has `process_status` enum: WAIT_FOR_DELIVERY, PENDING, PROCESSED, EXPIRED, REJECTED
- **ProductDetail references**:
  - Certificate batches: Only `batch_id` (single provider attribution)
  - Video batches: Both `batch_id` (pooled batch) and `sub_batch_id` (source provider)
- ProductDetail also references ProductGeneral and StorageTool; has `copy()` method for duplication
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

**ProductBatchProcessStatus** - Batch processing states (applies to both ProductBatch and ProductSubBatch):
- `WAIT_FOR_DELIVERY` - Order placed, waiting for provider delivery
- `PENDING` - Received, awaiting processing
- `PROCESSED` - Converted to product details
- `EXPIRED` - Past expiry date, cannot process
- `REJECTED` - Delivery rejected by warehouse staff

**ProviderVerificationType** - Provider verification method:
- `CERTIFICATE` - Verified via certificate/documentation
- `VIDEO` - Verified via video evidence

**RawProductDemandStatus** - Raw product demand lifecycle states:
- `PENDING` - Demand created, not yet fulfilled
- `PARTIALLY_FULFILLED` - Some batches received, demand partially met
- `FULFILLED` - All required batches received
- `CANCELLED` - Demand cancelled

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

**Product Entities** (product catalog and inventory with provider verification):
- `SubSubcategory` - Finest category granularity (e.g., "Thịt Gà", "Rau Muống")
- `ProductGeneral` - General product information (e.g., "Gà Ta Nguyên Con")
- `ProductBatch` - Batch entity representing received product from provider(s):
  - **CERTIFICATE verified**: Uniquely owned by single certified provider (e.g., VietGAP) - products show provider name on ecommerce
  - **VIDEO verified**: Shared/pooled batch from multiple providers - products shown without provider attribution
- `ProductSubBatch` - Individual provider deliveries for VIDEO-verified batches only (e.g., Provider B: 20kg + Provider C: 10kg → pooled into one ProductBatch)
- `ProductDetail` - Individual sellable product units:
  - From CERTIFICATE batch: Direct from ProductBatch, has provider attribution
  - From VIDEO batch: Created from ProductSubBatch, no provider attribution shown

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

**Event Flow Examples**:

**Certificate-Verified Provider Flow** (Provider A with VietGAP):
1. Back-office service publishes `subsubcategory-events` and `product-general-events`
2. Product storage service consumes and creates local entities
3. Provider A delivers 20kg "Thịt Heo" → Warehouse staff creates ProductBatch with verification_type=CERTIFICATE
4. Warehouse staff processes ProductBatch → creates 20 ProductDetails (1kg packs)
5. Service publishes `batch-detail-events` to ecommerce service
6. **Ecommerce displays**: "Thịt Heo 1kg - From Provider A (VietGAP Certified)"

**Video-Verified Provider Flow** (Provider B + C without certificates):
1. Back-office service publishes `subsubcategory-events` and `product-general-events`
2. Product storage service consumes and creates local entities
3. Create shared ProductBatch "batch-of-pig-1-20260429" with verification_type=VIDEO
4. Provider B delivers 20kg → Create ProductSubBatch, upload video, accept delivery
5. Provider C delivers 10kg → Create ProductSubBatch, upload video, accept delivery
6. Warehouse staff processes both ProductSubBatch entities → creates 30 ProductDetails (1kg packs) total
7. Service publishes `batch-detail-events` to ecommerce service
8. **Ecommerce displays**: "Thịt Heo 1kg" (no provider attribution - pooled product)

**Order Fulfillment Flow** (both types):
1. Ecommerce service publishes `order-pick-requested-events` when order is confirmed
2. Product storage service creates pick list and updates packaging progress via `order-packaging-progress-update-events`

### Batch Processing Logic

**Provider Verification-Based Workflows**:

The system handles two distinct workflows based on provider verification type:

#### 1. CERTIFICATE-Verified Providers (e.g., VietGAP, GlobalGAP)
**Example**: Provider A with VietGAP certificate delivers 20kg of "Thịt Heo"

**Workflow**:
1. Create ProductBatch directly with `verification_type = CERTIFICATE`, `provider_id = A`
2. **No ProductSubBatch needed** - batch is uniquely owned by the certified provider
3. Process ProductBatch → creates 20 ProductDetails (1kg packs)
4. ProductDetails reference only `batch_id` (no `sub_batch_id`)
5. **Ecommerce display**: Products shown with provider attribution (e.g., "From Provider A - VietGAP Certified")

#### 2. VIDEO-Verified Providers (no certificates)
**Example**: Provider B (20kg) and Provider C (10kg) both deliver "Thịt Heo" on 2026-04-29

**Workflow**:
1. Create shared ProductBatch: `batch-of-pig-1-20260429` with `verification_type = VIDEO`
2. Create ProductSubBatch for Provider B: 20kg, `product_batch_id = batch-of-pig-1-20260429`
3. Create ProductSubBatch for Provider C: 10kg, `product_batch_id = batch-of-pig-1-20260429`
4. Warehouse staff accepts/rejects each delivery independently via ProductSubBatchService
5. Process each ProductSubBatch → creates ProductDetails:
   - 20 ProductDetails from Provider B's sub-batch (1kg packs)
   - 10 ProductDetails from Provider C's sub-batch (1kg packs)
   - Total: 30 ProductDetails all linked to `batch-of-pig-1-20260429`
6. ProductDetails reference both `batch_id` (shared batch) and `sub_batch_id` (source provider)
7. **Ecommerce display**: Products shown without provider attribution (pooled product)

**Delivery Acceptance Workflow** (ProductSubBatchService - VIDEO batches only):
- `acceptDelivery(subBatchId, actualQuantity, note)` - Accept delivery, update quantity to actual received, set status to PENDING
- `rejectDelivery(subBatchId, note)` - Reject delivery, set status to REJECTED
- `uploadProofImages(subBatchId, images)` - Upload video proof for verification
- `findByProductBatchId(batchId)` - Get all sub-batches for a shared batch

**Product Detail Creation** (ProductDetailService.processProductBatch()):

**For CERTIFICATE batches** - Process ProductBatch directly:
1. Validates ProductBatch: process_status = PENDING, not expired, matching sub_subcategory_id
2. Unit conversion: Calculate ProductDetails (e.g., 20kg batch ÷ 1kg pack = 20 units)
3. Creates ProductDetails with only `batch_id` reference (provider attribution)
4. Updates ProductBatch: Sets `process_status = PROCESSED`
5. Publishes `BatchDetailCreateEvent` to Kafka

**For VIDEO batches** - Process ProductSubBatch:
1. Validates ProductSubBatch: process_status = PENDING, not expired, matching sub_subcategory_id
2. Unit conversion: Calculate ProductDetails (e.g., 20kg sub-batch ÷ 1kg pack = 20 units)
3. Creates ProductDetails with both `batch_id` (shared) and `sub_batch_id` (source provider)
4. Updates ProductSubBatch: Sets `process_status = PROCESSED`
5. Publishes `BatchDetailCreateEvent` to Kafka

**Auto-calculates Expiry**: Uses `avg_shelf_days` from SubSubcategory to set batch/sub-batch expiry date

### Service Layer Patterns

**Available Services**:
- WarehouseService - Warehouse CRUD operations
- StorageToolService - Storage tool CRUD operations
- RackService - Rack-specific CRUD operations
- RackLevelService - Rack level CRUD operations
- FridgeService - Fridge-specific CRUD operations
- ProductGeneralService - Product general CRUD + `getSuitableForBatch()`
- ProductBatchService - Product batch CRUD operations (parent batch entity)
- **ProductSubBatchService** - Product sub-batch CRUD + delivery acceptance/rejection + proof image uploads
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

**ProductSubBatchService** (VIDEO-verified batches only):
- `findByProductBatchId(Long productBatchId)` - Get all sub-batches for a shared/pooled ProductBatch
- `acceptDelivery(Long subBatchId, Long actualQuantity, String note)` - Accept delivery from provider, update quantity to actual received, set status to PENDING
- `rejectDelivery(Long subBatchId, String note)` - Reject delivery from provider, set status to REJECTED
- `uploadProofImages(Long subBatchId, List<MultipartFile> images)` - Upload video proof for verification
- `getProofImages(Long subBatchId)` - Retrieve proof video URLs for a sub-batch

**ProductDetailService**:
- `processProductBatch(ProcessProductBatchRequest)` - Core batch processing logic (see Batch Processing Logic section)
- **Two workflows based on verification_type**:
  - CERTIFICATE batches: Processes ProductBatch directly, sets only `batch_id` (provider attribution)
  - VIDEO batches: Processes ProductSubBatch, sets both `batch_id` (shared) and `sub_batch_id` (source provider)
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
- `/api/product-batch` - Product batch operations (parent batch entity)
- **`/api/product-sub-batch`** - Product sub-batch operations (individual deliveries)
- `/api/product-detail` - Product detail management
- `/api/order-items` - Order item tracking
- `/api/pick-list` - Pick list management (no CRUD, specialized endpoints)

**Special Endpoints**:

**ProductSubBatch Endpoints** (VIDEO-verified batches only):
- `GET /api/product-sub-batch/by-batch/{batchId}` - Get all sub-batches for a shared/pooled ProductBatch
- `GET /api/product-sub-batch/{subBatchId}/proof-images` - Get proof videos for a sub-batch
- `POST /api/product-sub-batch/{subBatchId}/proof-images` - Upload proof videos (multipart/form-data)
- `POST /api/product-sub-batch/{subBatchId}/accept-delivery` - Accept delivery from provider (body: {actualQuantity, note})
- `POST /api/product-sub-batch/{subBatchId}/reject-delivery` - Reject delivery from provider (body: {note})

**ProductDetail Endpoints**:
- `POST /api/product-detail/process-batch` - Legacy batch processing endpoint (deprecated, use process-batch-v2)
- **`POST /api/product-detail/process-batch-v2`** - **Recommended** UX/UI friendly batch processing endpoint:
  - **Automatically detects** CERTIFICATE vs VIDEO verification
  - **CERTIFICATE batch**: Creates product details with provider attribution, publishes event with provider info
  - **VIDEO batch**: Distributes product details proportionally across sub-batches, publishes event without provider info
  - **Request**: `{batchId, productGeneralId, price, storageToolId, numOfStar?}`
  - **Response**: Detailed breakdown with verification type, total created, and sub-batch distribution (for VIDEO)
- `GET /api/product-detail/quantity/{batchId}` - Get current quantity of available product details for a batch

**PickList Endpoints**:
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
- `*NotFoundException` - Entity not found (Warehouse, StorageTool, Rack, RackLevel, Fridge, ProductGeneral, ProductBatch, **ProductSubBatch**, ProductDetail)
- `*AlreadyExistsException` - Duplicate entity creation attempt
- `SubSubcategoryMismatchException` - ProductGeneral and ProductSubBatch have different sub_subcategory_id
- `ProductBatchExpiredException` - Attempting to process an expired sub-batch
- `ProductBatchAlreadyProcessedException` - Attempting to reprocess an already processed batch
- `ProductSubBatchAlreadyProcessedException` - Attempting to reprocess an already processed sub-batch

All exceptions are thrown from service layer, not repositories.

### Lombok Usage
Entities use selective `@Getter`/`@Setter` annotations rather than class-level to control field access. Services use `@AllArgsConstructor` or `@RequiredArgsConstructor` for constructor injection.

### Entity-Specific Features

**ProductDetail**:
- Individual sellable product unit (e.g., 1kg pack of "Thịt Heo")
- Has `copy()` method for efficient duplication during batch processing
- Auto-manages `created_at` and `updated_at` timestamps via `@PrePersist` and `@PreUpdate`
- Status enum: `ProductStatus` (STORED, EXPIRED, PICKED, IN_TRANSIT, DELIVERED, RETURNED, DISPOSED)
- **References depend on source batch type**:
  - **From CERTIFICATE batch**: Only `batch_id` set, `sub_batch_id` is null
    - Example: 20 packs from Provider A (VietGAP) → all reference batch-of-A, displayed with "Provider A" on ecommerce
  - **From VIDEO batch**: Both `batch_id` (shared batch) and `sub_batch_id` (source provider) set
    - Example: 30 packs from batch-of-pig-1-20260429 (Provider B + C) → displayed without provider name

**ProductBatch**:
- Represents product batch from provider(s) with two distinct use cases:
  - **CERTIFICATE verified**: Uniquely owned by single certified provider (e.g., Provider A with VietGAP)
    - Processed directly into ProductDetails (no sub-batches)
    - Products displayed with provider attribution on ecommerce
  - **VIDEO verified**: Shared/pooled batch from multiple providers (e.g., Provider B + C)
    - Contains multiple ProductSubBatch entities (1-to-N relationship)
    - Products displayed without provider attribution (pooled)
- Has `process_status` field: WAIT_FOR_DELIVERY, PENDING, PROCESSED, EXPIRED, REJECTED
- Auto-initializes to PENDING in constructor
- Has `verification_type`: CERTIFICATE (single provider) or VIDEO (pooled providers)
- Contains `provider_id`: Set for CERTIFICATE batches, null/generic for VIDEO batches
- Contains `sub_subcategory_id` for validation against ProductGeneral
- Tracks `raw_product_demand_id` for linking to demand management
- Contains `proof_image_urls` collection for verification (certificate documents or video evidence)

**ProductSubBatch** (VIDEO-verified batches only):
- Represents individual provider delivery within a shared/pooled ProductBatch
- **Only used for VIDEO-verified batches** - allows multiple providers to contribute to one batch
- Example: Provider B delivers 20kg + Provider C delivers 10kg → both become sub-batches of "batch-of-pig-1-20260429"
- Has `process_status` field: WAIT_FOR_DELIVERY, PENDING, PROCESSED, EXPIRED, REJECTED
- Auto-initializes to PENDING in constructor
- Contains `product_batch_id` to reference parent ProductBatch (shared batch)
- Contains `provider_id` to track which provider delivered this sub-batch
- Contains `sub_subcategory_id` for validation against ProductGeneral
- Tracks `raw_product_demand_id` for linking to demand management
- Contains `proof_image_urls` collection for video verification
- **Gets processed into ProductDetails** (each sub-batch processed independently)

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
- Must have matching `sub_subcategory_id` with ProductSubBatch for processing

### CORS Configuration
Frontend origins are configured in `WebConfig.java`. Add new origins there when deploying to new environments.

### Primary Keys
- ProductGeneral uses manually assigned IDs (`@Id` without `@GeneratedValue`) - ID comes from back-office service
- SubSubcategory uses manually assigned IDs (`@Id` without `@GeneratedValue`) - ID comes from back-office service
- ProductBatch, ProductSubBatch, ProductDetail, and other entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment

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

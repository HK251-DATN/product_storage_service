# Back-Office UI: Product Demand & Delivery Acceptance

## Service Base URLs

| Service                     | Base URL                 | Purpose                                                     |
|-----------------------------|--------------------------|-------------------------------------------------------------|
| **Product Storage Service** | `http://<host>:9200/api` | RawProductDemand CRUD, batch delivery acceptance            |
| **Back-Office Service**     | `http://<host>:9100/api` | Categories (sub-subcategories), Providers, Product Generals |

All requests require authentication via JWT. Include the token in the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

## Response Format

Every response wraps data in an `ApiResponse<T>` object:

```json
{
  "type": "GOOD" | "ERROR" | "WARN" | "SKIP_AS_GOOD",
  "code": "string",
  "message": "string",
  "detail": <T> | null,
  "timestamp": "2026-04-29T10:00:00"
}
```

- **type**: `GOOD` = success, `ERROR` = failure, `SKIP_AS_GOOD` = empty result (not an error), `WARN` = warning
- **detail**: Contains the actual data object or null
- Check `type` first to determine success/failure, then read `detail`

---

## Flow 1: Admin Creates Product Demand

### Step 1.1: Create a Demand

**POST** `/api/raw-product-demand`

**Request Body**:
```json
{
  "subSubcategoryId": 7,
  "unit": "KILOGRAM",
  "unitQuantity": 500,
  "unitPrice": 85000,
  "dateNeed": "2026-05-05",
  "note": "Cần thịt heo tươi cho tuần sau"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `subSubcategoryId` | Long | Yes | SubSubcategory ID (e.g., 7 = "Thịt Heo") |
| `unit` | String | Yes | Unit enum: `KILOGRAM`, `GRAM`, `PIECE`, `DOZEN`, `LITER`, `MILLILITER`, `PACK`, `BOX`, `BOTTLE` |
| `unitQuantity` | Long | Yes | Total quantity needed |
| `unitPrice` | Long | Yes | Budgeted price per unit (VND) |
| `dateNeed` | String | Yes | Deadline date (ISO 8601: `YYYY-MM-DD`) |
| `note` | String | No | Additional notes for providers |

**Success Response** (HTTP 200):
```json
{
  "type": "GOOD",
  "code": "201",
  "message": "Raw product demand created",
  "detail": {
    "demandId": 1,
    "subSubcategoryId": 7,
    "unit": "KILOGRAM",
    "unitQuantity": 500,
    "unitPrice": 85000,
    "currentProgress": 0,
    "dateNeed": "2026-05-05",
    "status": "PENDING",
    "note": "Cần thịt heo tươi cho tuần sau",
    "createdAt": "2026-04-29T10:00:00",
    "updatedAt": "2026-04-29T10:00:00"
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

**Error Response** (HTTP 500):
```json
{
  "type": "ERROR",
  "code": "500",
  "message": "Error message here",
  "detail": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### Step 1.2: List All Demands (Paginated)

**GET** `/api/raw-product-demand?pageNum=1&pageSize=20`

**Query Parameters**:
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `pageNum` | Integer | 1 | Page number (1-based) |
| `pageSize` | Integer | 20 | Items per page |

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "OK",
  "message": "Raw product demands retrieved",
  "detail": [
    {
      "demandId": 1,
      "subSubcategoryId": 7,
      "unit": "KILOGRAM",
      "unitQuantity": 500,
      "unitPrice": 85000,
      "currentProgress": 20,
      "dateNeed": "2026-05-05",
      "status": "PARTIALLY_FULFILLED",
      "note": "Cần thịt heo tươi cho tuần sau"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

**Empty Response** (HTTP 200, no demands yet):
```json
{
  "type": "SKIP_AS_GOOD",
  "code": "OK",
  "message": "No raw product demands exist",
  "detail": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### Step 1.3: Get Demand by ID

**GET** `/api/raw-product-demand/{demandId}`

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "OK",
  "message": "Raw product demand retrieved",
  "detail": {
    "demandId": 1,
    "subSubcategoryId": 7,
    "unit": "KILOGRAM",
    "unitQuantity": 500,
    "unitPrice": 85000,
    "currentProgress": 40,
    "dateNeed": "2026-05-05",
    "status": "PARTIALLY_FULFILLED",
    "note": "Cần thịt heo tươi cho tuần sau"
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### Step 1.4: List Demands by Category

**GET** `/api/raw-product-demand/by-category/{subSubcategoryId}`

Use this to filter demands for a specific product category (e.g., only "Thịt Heo" demands).

**Success Response**: Same structure as Step 1.2, but filtered list.

---

### Step 1.5: Update a Demand

**PUT** `/api/raw-product-demand/{demandId}`

**Request Body** (all fields optional — partial update):
```json
{
  "unitQuantity": 600,
  "dateNeed": "2026-05-10",
  "note": "Updated: need more quantity"
}
```

---

### Step 1.6: Delete a Demand

**DELETE** `/api/raw-product-demand/{demandId}`

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "OK",
  "message": "Raw product demand deleted",
  "detail": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### Demand Status Values

| Status                | Description                                        |
|-----------------------|----------------------------------------------------|
| `PENDING`             | No provider has confirmed yet                      |
| `PARTIALLY_FULFILLED` | Some providers confirmed, but total < unitQuantity |
| `FULFILLED`           | Providers confirmed >= unitQuantity total          |
| `CANCELLED`           | Demand cancelled by admin                          |

---

## Flow 3: Admin Receives Product Batch — Accept/Reject Delivery

### Step 3.1: View Batches Awaiting Delivery

**GET** `/api/product-batch/status/WAIT_FOR_DELIVERY`

This returns all batches (from certificate providers and shared video-provider batches) that are awaiting physical delivery.

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Batches retrieved by status",
  "detail": [
    {
      "batchId": 101,
      "quantity": 20,
      "unit": "KILOGRAM",
      "note": "Provider A delivery",
      "receivedAt": "2026-04-29T09:00:00",
      "expiredAt": "2026-05-02T09:00:00",
      "processStatus": "WAIT_FOR_DELIVERY",
      "providerId": 1,
      "subSubcategoryId": 7,
      "verificationType": "CERTIFICATE",
      "rawProductDemandId": 1,
      "proofImageUrls": [],
      "createdAt": "2026-04-29T09:00:00",
      "updatedAt": "2026-04-29T09:00:00"
    },
    {
      "batchId": 102,
      "quantity": 30,
      "unit": "KILOGRAM",
      "note": "Shared batch — Provider B + C",
      "receivedAt": "2026-04-29T10:00:00",
      "expiredAt": "2026-05-02T10:00:00",
      "processStatus": "WAIT_FOR_DELIVERY",
      "providerId": null,
      "subSubcategoryId": 7,
      "verificationType": "VIDEO",
      "rawProductDemandId": 1,
      "proofImageUrls": [],
      "createdAt": "2026-04-29T10:00:00",
      "updatedAt": "2026-04-29T10:00:00"
    }
  ],
  "timestamp": "2026-04-29T12:00:00"
}
```

**Key Fields to Display in UI**:
- `verificationType`: `CERTIFICATE` = single provider (display provider name), `VIDEO` = merged providers (no provider attribution on product listing)
- `providerId`: `null` means this is a shared video-provider batch
- `rawProductDemandId`: Links back to which demand this batch belongs to
- `processStatus`: Only batches with `WAIT_FOR_DELIVERY` should appear in the "pending delivery" table

---

### Step 3.2: View Sub-Batches for a Shared Video Batch

If the batch has `verificationType: "VIDEO"` and `providerId: null`, fetch its sub-batches to see individual provider contributions:

**GET** `/api/product-sub-batch/by-batch/{batchId}`

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "OK",
  "message": "Sub batches retrieved for batch",
  "detail": [
    {
      "subBatchId": 201,
      "quantity": 20,
      "unit": "KILOGRAM",
      "note": "Provider B delivery",
      "processStatus": "WAIT_FOR_DELIVERY",
      "providerId": 2,
      "subSubcategoryId": 7,
      "productBatchId": 102,
      "rawProductDemandId": 1,
      "proofImageUrls": []
    },
    {
      "subBatchId": 202,
      "quantity": 10,
      "unit": "KILOGRAM",
      "note": "Provider C delivery",
      "processStatus": "WAIT_FOR_DELIVERY",
      "providerId": 3,
      "subSubcategoryId": 7,
      "productBatchId": 102,
      "rawProductDemandId": 1,
      "proofImageUrls": []
    }
  ],
  "timestamp": "2026-04-29T12:00:00"
}
```

---

### Step 3.3: Accept a Certificate Provider Batch

**POST** `/api/product-batch/{batchId}/accept-delivery`

**Request Body**:
```json
{
  "actualQuantity": 19,
  "note": "Received 19kg instead of 20kg, quality OK"
}
```

| Field            | Type   | Required | Description                                                    |
|------------------|--------|----------|----------------------------------------------------------------|
| `actualQuantity` | Long   | Yes      | Actual delivered quantity (can differ from confirmed quantity) |
| `note`           | String | No       | Delivery inspection notes                                      |

**Success Response** (batch status changes from `WAIT_FOR_DELIVERY` → `PENDING`):
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Delivery accepted",
  "detail": {
    "batchId": 101,
    "quantity": 19,
    "unit": "KILOGRAM",
    "note": "Received 19kg instead of 20kg, quality OK",
    "processStatus": "PENDING",
    "providerId": 1,
    "verificationType": "CERTIFICATE",
    "rawProductDemandId": 1
  },
  "timestamp": "2026-04-29T14:00:00"
}
```

**Error Response** (wrong status):
```json
{
  "type": "ERROR",
  "code": "400",
  "message": "Batch is in status PROCESSED, cannot accept delivery. Expected WAIT_FOR_DELIVERY",
  "detail": null,
  "timestamp": "2026-04-29T14:00:00"
}
```

**What happens on accept**:
- Batch `processStatus` changes to `PENDING` (ready for processing into product details)
- If `actualQuantity` differs from the confirmed quantity, the linked demand's `currentProgress` is adjusted accordingly
- Demand status auto-updates: `PENDING` / `PARTIALLY_FULFILLED` / `FULFILLED`

---

### Step 3.4: Accept a Shared Video Batch (Accepts All Sub-Batches)

**POST** `/api/product-batch/{batchId}/accept-delivery`

Same endpoint as Step 3.3. When a shared video batch is accepted:

- The parent batch status changes to `PENDING`
- **All sub-batches** within that batch also change to `PENDING`
- `actualQuantity` is applied to the parent batch total
- Demand progress is adjusted based on quantity difference

**Request Body**:
```json
{
  "actualQuantity": 28,
  "note": "Total received 28kg from B + C combined"
}
```

---

### Step 3.5: Accept an Individual Sub-Batch

If you want to accept/reject sub-batches individually (e.g., Provider B's delivery is OK but Provider C's is not):

**POST** `/api/product-sub-batch/{subBatchId}/accept-delivery`

**Request Body**:
```json
{
  "actualQuantity": 19,
  "note": "Provider B delivered 19kg, quality good"
}
```

**What happens**:
- Sub-batch status changes to `PENDING`
- Parent batch quantity is adjusted (`+delta`)
- If the parent batch was `WAIT_FOR_DELIVERY`, it also transitions to `PENDING`
- Demand progress is adjusted

---

### Step 3.6: Reject a Certificate Provider Batch

**POST** `/api/product-batch/{batchId}/reject-delivery`

**Request Body**:
```json
{
  "note": "Product quality does not meet standards, rejected"
}
```

**Success Response** (batch status changes to `REJECTED`):
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Delivery rejected",
  "detail": {
    "batchId": 101,
    "processStatus": "REJECTED",
    "note": "Product quality does not meet standards, rejected",
    "quantity": 20,
    "providerId": 1,
    "verificationType": "CERTIFICATE"
  },
  "timestamp": "2026-04-29T14:00:00"
}
```

**What happens on reject**:
- Batch `processStatus` changes to `REJECTED`
- The batch's full quantity is **subtracted** from the demand's `currentProgress`
- Demand status may downgrade: `FULFILLED` → `PARTIALLY_FULFILLED` → `PENDING`

---

### Step 3.7: Reject an Individual Sub-Batch

**POST** `/api/product-sub-batch/{subBatchId}/reject-delivery`

**Request Body**:
```json
{
  "note": "Provider C's portion spoiled during transit"
}
```

**What happens**:
- Sub-batch status changes to `REJECTED`
- Parent batch quantity is reduced by the sub-batch's quantity
- If **all** sub-batches of a parent batch are rejected, the parent batch also becomes `REJECTED`
- Demand progress is reduced

---

### Step 3.8: View Batches by Other Statuses

Use the same endpoint to filter batches by any status:

| Endpoint                                  | Use Case                                          |
|-------------------------------------------|---------------------------------------------------|
| `GET /api/product-batch/status/PENDING`   | Batches ready for processing into product details |
| `GET /api/product-batch/status/PROCESSED` | Already processed batches                         |
| `GET /api/product-batch/status/REJECTED`  | Rejected deliveries for review                    |
| `GET /api/product-batch/status/EXPIRED`   | Expired batches                                   |

---

### Batch Process Status Values

| Status              | Description                                                       |
|---------------------|-------------------------------------------------------------------|
| `WAIT_FOR_DELIVERY` | Provider confirmed, awaiting physical delivery                    |
| `PENDING`           | Delivered and accepted, ready for processing into product details |
| `PROCESSED`         | Already split into individual product detail units                |
| `EXPIRED`           | Past expiry date, cannot be processed                             |
| `REJECTED`          | Delivery rejected by admin                                        |

---

## Complete Flow Example

### Scenario: Demand for 50kg Thịt Heo

1. **Admin creates demand** → `POST /api/raw-product-demand`
   - Response: demand created with `status: PENDING`, `currentProgress: 0`

2. **Provider A (certificate) confirms 30kg** → handled by provider UI
   - Demand auto-updates: `status: PARTIALLY_FULFILLED`, `currentProgress: 30`

3. **Provider B (video) confirms 20kg** → handled by provider UI
   - Demand auto-updates: `status: FULFILLED`, `currentProgress: 50`

4. **Admin views pending deliveries** → `GET /api/product-batch/status/WAIT_FOR_DELIVERY`
   - Sees batch #101 (certificate, 30kg, provider A) and batch #102 (video, 20kg, shared)

5. **Admin accepts batch #101 with actual 29kg** → `POST /api/product-batch/101/accept-delivery`
   - Batch #101: status → `PENDING`, quantity updated to 29
   - Demand: `currentProgress` adjusts from 50 → 49, status downgrades to `PARTIALLY_FULFILLED`

6. **Admin rejects sub-batch #202 (Provider C's portion)** → `POST /api/product-sub-batch/202/reject-delivery`
   - Sub-batch #202: status → `REJECTED`
   - Parent batch #102 quantity reduced
   - Demand progress further reduced

7. **Admin processes accepted batches** → `POST /api/product-detail/process-batch` (existing endpoint)
   - Batches in `PENDING` status are split into sellable product units

---

## Helper APIs from Back-Office Service (`http://<host>:9100/api`)

These APIs live in the back-office service and are needed for UI forms, dropdowns, and reference data.

### A. Sub-Subcategories (for demand creation dropdown)

When creating a RawProductDemand, the admin needs to select a product category. The full 3-level category hierarchy is:

```
Category (level 1) → Subcategory (level 2) → SubSubcategory (level 3)
```

The `subSubcategoryId` field on RawProductDemand references the **level 3** item.

#### A.1: Get All Categories (Level 1)

**GET** `/api/categories`

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Categories retrieved successfully",
  "detail": [
    {
      "categoryId": 1,
      "name": "Thực Phẩm Tươi Sống",
      "description": "Fresh food products",
      "iconUrl": "...",
      "parentId": null
    },
    {
      "categoryId": 2,
      "name": "Đồ Uống",
      "description": "Beverages",
      "iconUrl": "...",
      "parentId": null
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

#### A.2: Get Subcategories Under a Category (Level 2)

**GET** `/api/categories/{categoryId}/subcategories`

**Example**: Get subcategories of "Thực Phẩm Tươi Sống" (id=1):
```
GET /api/categories/1/subcategories
```

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Subcategories retrieved successfully",
  "detail": [
    {
      "categoryId": 2,
      "name": "Thịt",
      "description": "Meat products",
      "iconUrl": "...",
      "parentId": 1
    },
    {
      "categoryId": 4,
      "name": "Hải Sản",
      "description": "Seafood products",
      "iconUrl": "...",
      "parentId": 1
    },
    {
      "categoryId": 6,
      "name": "Rau Củ Quả",
      "description": "Vegetables and fruits",
      "iconUrl": "...",
      "parentId": 1
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

#### A.3: Get All Sub-Subcategories (Level 3) — Flat List

**GET** `/api/categories/sub-subcategories`

Returns all sub-subcategories across the entire system. Use this for a flat dropdown when creating demands.

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Sub-subcategory retrieved successfully",
  "detail": [
    {
      "subSubcategoryId": 1,
      "name": "Thịt Gà",
      "description": "Thịt gà tươi nguyên con và các phần",
      "iconUrl": "...",
      "subcategoryId": 2,
      "avgShelfDays": 3
    },
    {
      "subSubcategoryId": 7,
      "name": "Thịt Heo",
      "description": "Thịt heo tươi các loại",
      "iconUrl": "...",
      "subcategoryId": 3,
      "avgShelfDays": 3
    },
    {
      "subSubcategoryId": 11,
      "name": "Tôm Tươi",
      "description": "Tôm sú, tôm thẻ tươi sống",
      "iconUrl": "...",
      "subcategoryId": 4,
      "avgShelfDays": 2
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

#### A.4: Get Sub-Subcategories Under a Subcategory (Level 2 → Level 3)

**GET** `/api/categories/{subcategoryId}/sub-subcategories`

**Example**: Get all meat types under "Thịt" (subcategoryId=3):
```
GET /api/categories/3/sub-subcategories
```

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Sub-subcategories retrieved successfully",
  "detail": [
    {
      "subSubcategoryId": 6,
      "name": "Thịt Bò",
      "description": "Thịt bò tươi các loại",
      "subcategoryId": 3,
      "avgShelfDays": 4
    },
    {
      "subSubcategoryId": 7,
      "name": "Thịt Heo",
      "description": "Thịt heo tươi các loại",
      "subcategoryId": 3,
      "avgShelfDays": 3
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

#### A.5: Get Single Sub-Subcategory by ID

**GET** `/api/categories/sub-subcategories/{subSubcategoryId}`

**Example**: `GET /api/categories/sub-subcategories/7`

**Key Fields for UI**:
- `subSubcategoryId`: The ID to use in RawProductDemand's `subSubcategoryId` field
- `name`: Display name (e.g., "Thịt Heo")
- `avgShelfDays`: Useful for estimating expiry dates when creating demands

---

### B. Providers (for linking demands and viewing provider info)

#### B.1: List All Providers (Paginated)

**GET** `/api/provider?pageNum=1&pageSize=20`

**Optional Query Parameters**:

| Param    | Type   | Description                                                       |
|----------|--------|-------------------------------------------------------------------|
| `status` | String | Filter by `VerificationStatus`: `PENDING`, `APPROVED`, `REJECTED` |

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Read all providers successfully",
  "detail": [
    {
      "providerId": 1,
      "userId": 100,
      "verificationStatus": "APPROVED",
      "verificationMethod": "CERTIFICATE",
      "description": "VietGAP certified farm",
      "createdAt": "2026-01-15T08:00:00",
      "updatedAt": "2026-02-01T10:00:00"
    },
    {
      "providerId": 2,
      "userId": 101,
      "verificationStatus": "APPROVED",
      "verificationMethod": "VIDEO",
      "description": "Video-verified local farm",
      "createdAt": "2026-03-10T08:00:00",
      "updatedAt": "2026-03-20T10:00:00"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

**Empty Response**:
```json
{
  "type": "SKIP_AS_GOOD",
  "code": "200",
  "message": "No provider found",
  "detail": null,
  "timestamp": "2026-04-29T10:00:00"
}
```

#### B.2: Get Provider Detail (with user info)

**GET** `/api/provider/{providerId}`

Returns provider info combined with the linked user account details.

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Read provider successfully",
  "detail": {
    "providerId": 1,
    "userId": 100,
    "verificationStatus": "APPROVED",
    "verificationMethod": "CERTIFICATE",
    "description": "VietGAP certified farm",
    "user": {
      "userId": 100,
      "email": "providerA@example.com",
      "fullName": "Nguyễn Văn A",
      "phone": "0901234567",
      "address": "123 Đường ABC, Quận 1, TP.HCM"
    }
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

#### B.3: Get Provider's Certificates

**GET** `/api/provider/{providerId}/certificates`

Use this to view uploaded certificates for certificate-verified providers.

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Get certificates successfully",
  "detail": [
    {
      "certificateId": 10,
      "providerId": 1,
      "certificateType": "VIETGAP",
      "documentUrl": "https://pub-b72d8c021b3848f8b4d8805e93e18af6.r2.dev/cert-vietgap-abc.pdf",
      "issueDate": "2025-06-15",
      "expiryDate": "2027-06-15",
      "status": "APPROVED"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

#### B.4: Get Provider's Verification Videos

**GET** `/api/provider/{providerId}/videos`

Use this to view uploaded verification videos for video-verified providers.

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Get videos successfully",
  "detail": [
    {
      "videoId": 20,
      "providerId": 2,
      "videoType": "FARM_TOUR",
      "videoUrl": "https://pub-1b4c6325308a40f68dc9dca3d1771fbd.r2.dev/farm-tour-video.mp4",
      "status": "APPROVED",
      "uploadedAt": "2026-03-10T08:00:00"
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

---

### C. Product Generals (for reference when browsing products)

#### C.1: List All Product Generals

**GET** `/api/product-general?pageNum=1&pageSize=20`

Returns all product general entries (the product types that will be created from processed batches).

**Success Response**:
```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Get all product generals successfully",
  "detail": [
    {
      "prodGenId": 19,
      "name": "Thịt Heo Ba Chỉ",
      "description": "Thịt heo ba chỉ tươi, vừa nạc vừa mỡ",
      "imgUrl": "https://pub-0365edd1781141cdb68675969c7cdb87.r2.dev/pork-belly.jpg",
      "subSubcategoryId": 7,
      "unit": "GRAM",
      "unitQuantity": 500
    },
    {
      "prodGenId": 20,
      "name": "Thịt Heo Nạc Vai",
      "description": "Thịt heo nạc vai tươi, ít mỡ",
      "imgUrl": "...",
      "subSubcategoryId": 7,
      "unit": "GRAM",
      "unitQuantity": 500
    }
  ],
  "timestamp": "2026-04-29T10:00:00"
}
```

**Key Fields**:
- `prodGenId`: Used in `ProcessProductBatchRequest` when processing a batch
- `subSubcategoryId`: Links back to the sub-subcategory (same ID used in RawProductDemand)
- `unit`: The unit this product is sold in (e.g., `GRAM` = 500g packages)
- `unitQuantity`: The package size (e.g., 500 = 500g per package)

---

## Suggested UI Page Structure

### Page 1: Raw Product Demands Management

```
┌─────────────────────────────────────────────────────────┐
│ Raw Product Demands                                    │
├─────────────────────────────────────────────────────────┤
│ [Create New Demand]                                    │
│                                                        │
│ ┌───┬─────────────┬─────────┬────────┬────────┬──────┐ │
│ │ # │ Category    │ Qty     │ Need   │ Status │ Act  │ │
│ ├───┼─────────────┼─────────┼────────┼────────┼──────┤ │
│ │ 1 │ Thịt Heo    │ 500 kg  │ May 05 │ PARTIAL│ ✏️🗑️ │ │
│ │ 2 │ Thịt Bò     │ 300 kg  │ May 10 │ PENDING│ ✏️🗑️ │ │
│ │ 3 │ Tôm Tươi    │ 200 kg  │ May 03 │ FULFILL│ ✏️🗑️ │ │
│ └───┴─────────────┴─────────┴────────┴────────┴──────┘ │
│ Page: 1 2 3 ...                                        │
└─────────────────────────────────────────────────────────┘
```

**Data Sources**:
- Category names: fetch from `GET /api/categories/sub-subcategories` (back-office), map `subSubcategoryId` to name
- Status values: from the product-storage service RawProductDemand entity

### Page 2: Create/Edit Demand Form

```
┌─────────────────────────────────────────────────────────┐
│ Create Raw Product Demand                              │
├─────────────────────────────────────────────────────────┤
│ Category:    [Thịt Heo ▼]   ← populated from            │
│                              GET /api/categories/        │
│                              sub-subcategories           │
│                                                        │
│ Quantity:    [500     ] KILOGRAM                        │
│ Unit Price:  [85000   ] VND/unit                        │
│ Need By:     [2026-05-05]                               │
│ Note:        [Cần thịt heo tươi cho tuần sau...]        │
│                                                        │
│              [Cancel]  [Create]                         │
└─────────────────────────────────────────────────────────┘
```

### Page 3: Pending Deliveries

```
┌─────────────────────────────────────────────────────────┐
│ Pending Deliveries (WAIT_FOR_DELIVERY)                 │
├─────────────────────────────────────────────────────────┤
│ ┌───┬─────────────┬─────────┬────────────┬──────────┐  │
│ │ # │ Category    │ Qty     │ Provider   │ Status   │  │
│ ├───┼─────────────┼─────────┼────────────┼──────────┤  │
│ │101│ Thịt Heo    │ 20 kg   │ Provider A │ ⏳ Wait  │  │
│ │   │             │         │ [CERT]     │          │  │
│ │102│ Thịt Heo    │ 30 kg   │ B + C      │ ⏳ Wait  │  │
│ │   │             │         │ [VIDEO]    │          │  │
│ └───┴─────────────┴─────────┴────────────┴──────────┘  │
│                                                        │
│ Click a row to expand sub-batches (for VIDEO batches): │
│   └─ Sub-batch #201: 20kg - Provider B                │
│   └─ Sub-batch #202: 10kg - Provider C                │
│                                                        │
│ Actions per row: [✅ Accept] [❌ Reject]               │
└─────────────────────────────────────────────────────────┘
```

**Data Sources**:
- `GET /api/product-batch/status/WAIT_FOR_DELIVERY` (product-storage) for the main table
- `GET /api/provider/{providerId}` (back-office) to resolve provider names
- `GET /api/product-sub-batch/by-batch/{batchId}` (product-storage) for sub-batch details
- `GET /api/categories/sub-subcategories/{id}` (back-office) for category names

### Page 4: Accepted Deliveries (PENDING / PROCESSED)

```
┌─────────────────────────────────────────────────────────┐
│ Delivery History                                       │
├─────────────────────────────────────────────────────────┤
│ Filter: [All ▼] [PENDING ▼] [PROCESSED ▼] [REJECTED ▼] │
│                                                        │
│ ┌───┬─────────────┬─────────┬────────────┬──────────┐  │
│ │ # │ Category    │ Qty     │ Provider   │ Status   │  │
│ ├───┼─────────────┼─────────┼────────────┼──────────┤  │
│ │101│ Thịt Heo    │ 19 kg   │ Provider A │ ✅ Ready │  │
│ │103│ Thịt Bò     │ 50 kg   │ Provider D │ ✅ Done  │  │
│ │104│ Tôm Tươi    │ 15 kg   │ B + C      │ ❌ Rej.  │  │
│ └───┴─────────────┴─────────┴────────────┴──────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Data Sources**:
- `GET /api/product-batch/status/PENDING`
- `GET /api/product-batch/status/PROCESSED`
- `GET /api/product-batch/status/REJECTED`

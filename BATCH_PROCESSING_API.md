# Batch Processing API - UX/UI Guide

## Overview

The `/api/product-detail/process-batch-v2` endpoint intelligently processes ProductBatch into sellable ProductDetails, automatically handling both **CERTIFICATE-verified** and **VIDEO-verified** batches.

## Key Features

✅ **Automatic Detection** - No need to specify verification type  
✅ **Single Request** - Works for both single-provider and multi-provider batches  
✅ **Detailed Response** - Clear breakdown of what was created  
✅ **Provider Attribution** - Automatically handles provider info in events

---

## API Endpoint

**POST** `/api/product-detail/process-batch-v2`

### Request Body

```json
{
  "batchId": 1,              // ProductBatch ID to process
  "productGeneralId": 5,     // ProductGeneral ID (product type)
  "price": 50000,            // Price per unit (VND)
  "storageToolId": 3,        // Where to store the products
  "numOfStar": 0             // Optional: Initial rating (default: 0)
}
```

**All fields are required except `numOfStar`**

---

## Response Examples

### Example 1: CERTIFICATE-Verified Batch (Provider A - VietGAP)

**Scenario**: Provider A (VietGAP certified) delivers 20kg "Thịt Heo"

**Request**:
```json
{
  "batchId": 101,
  "productGeneralId": 5,
  "price": 60000,
  "storageToolId": 3
}
```

**Response**:
```json
{
  "status": "SUCCESS",
  "message": "Batch processed successfully (CERTIFICATE verification): 20 product details created",
  "data": {
    "verificationType": "CERTIFICATE",
    "totalProductDetailsCreated": 20,
    "batchId": 101,
    "productGeneralId": 5,
    "providerId": 10,          // Provider A's ID
    "subBatchBreakdowns": []   // Empty for CERTIFICATE batches
  }
}
```

**What Happens**:
- ✅ 20 ProductDetails created (1kg each)
- ✅ Each ProductDetail has `batch_id = 101`, `sub_batch_id = null`
- ✅ BatchDetailCreateEvent published **WITH** provider info (providerId: 10, verificationType: "CERTIFICATE")
- ✅ **Ecommerce will display**: "Thịt Heo 1kg - From Provider A (VietGAP Certified)"

---

### Example 2: VIDEO-Verified Batch (Provider B + C)

**Scenario**: 
- Provider B delivers 20kg "Thịt Heo" (SubBatch ID: 201)
- Provider C delivers 10kg "Thịt Heo" (SubBatch ID: 202)
- Both pooled into batch "batch-of-pig-1-20260429" (Batch ID: 102)

**Request**:
```json
{
  "batchId": 102,
  "productGeneralId": 5,
  "price": 50000,
  "storageToolId": 4
}
```

**Response**:
```json
{
  "status": "SUCCESS",
  "message": "Batch processed successfully (VIDEO verification): 30 product details created",
  "data": {
    "verificationType": "VIDEO",
    "totalProductDetailsCreated": 30,
    "batchId": 102,
    "productGeneralId": 5,
    "providerId": null,        // No single provider (pooled)
    "subBatchBreakdowns": [
      {
        "subBatchId": 201,
        "providerId": 20,      // Provider B
        "quantityProcessed": 20,
        "productDetailsCreated": 20
      },
      {
        "subBatchId": 202,
        "providerId": 30,      // Provider C
        "quantityProcessed": 10,
        "productDetailsCreated": 10
      }
    ]
  }
}
```

**What Happens**:
- ✅ 30 ProductDetails created total
  - 20 ProductDetails with `batch_id = 102`, `sub_batch_id = 201` (from Provider B)
  - 10 ProductDetails with `batch_id = 102`, `sub_batch_id = 202` (from Provider C)
- ✅ BatchDetailCreateEvent published **WITHOUT** provider info (providerId: null, verificationType: "VIDEO")
- ✅ **Ecommerce will display**: "Thịt Heo 1kg" (no provider name - generic pooled product)

---

## UI/UX Implementation Guide

### Step 1: Batch Selection Screen

```
┌─────────────────────────────────────────┐
│ Select Batch to Process                 │
├─────────────────────────────────────────┤
│ ○ Batch #101 - Thịt Heo (20kg)         │
│   Provider: Provider A (VietGAP) ✓      │
│   Status: PENDING                        │
│                                          │
│ ○ Batch #102 - Thịt Heo (30kg)         │
│   Providers: Provider B, Provider C 📹   │
│   Status: PENDING                        │
└─────────────────────────────────────────┘
```

### Step 2: Processing Form

```
┌─────────────────────────────────────────┐
│ Process Batch #102                       │
├─────────────────────────────────────────┤
│ Product Type:                            │
│ [Dropdown: Thịt Heo (1kg pack)]         │
│                                          │
│ Price per unit (VND):                    │
│ [50,000]                                 │
│                                          │
│ Storage Location:                        │
│ [Dropdown: Fridge #4]                   │
│                                          │
│ [Process Batch]                          │
└─────────────────────────────────────────┘
```

### Step 3: Success Response (VIDEO Batch)

```
┌─────────────────────────────────────────┐
│ ✅ Batch Processed Successfully          │
├─────────────────────────────────────────┤
│ Verification Type: VIDEO (Pooled)       │
│ Total Products Created: 30 units         │
│                                          │
│ Breakdown by Provider:                   │
│ • Provider B: 20 units (20kg)           │
│ • Provider C: 10 units (10kg)           │
│                                          │
│ ℹ️  These products will be displayed     │
│    without provider attribution          │
└─────────────────────────────────────────┘
```

### Step 3: Success Response (CERTIFICATE Batch)

```
┌─────────────────────────────────────────┐
│ ✅ Batch Processed Successfully          │
├─────────────────────────────────────────┤
│ Verification Type: CERTIFICATE           │
│ Provider: Provider A (VietGAP)          │
│ Total Products Created: 20 units         │
│                                          │
│ ℹ️  These products will be displayed     │
│    with Provider A attribution           │
└─────────────────────────────────────────┘
```

---

## Error Handling

### Common Errors

**1. Batch Already Processed**
```json
{
  "status": "ERROR",
  "message": "Product batch is already PROCESSED",
  "data": null
}
```

**2. Batch Expired**
```json
{
  "status": "ERROR",
  "message": "Product batch has expired",
  "data": null
}
```

**3. Category Mismatch**
```json
{
  "status": "ERROR",
  "message": "SubSubcategory mismatch: ProductBatch has subSubcategoryId=5 but ProductGeneral has subSubcategoryId=7",
  "data": null
}
```

**4. VIDEO Batch Without Sub-Batches**
```json
{
  "status": "ERROR",
  "message": "VIDEO-verified batch must have at least one sub-batch",
  "data": null
}
```

---

## Business Logic Summary

| Verification Type | Provider Attribution | Event Data | Ecommerce Display |
|-------------------|---------------------|------------|-------------------|
| **CERTIFICATE** | ✅ Single provider | Includes `providerId`, `verificationType` | "From Provider A (VietGAP)" |
| **VIDEO** | ❌ Pooled/anonymous | `providerId: null`, `verificationType: VIDEO` | Generic product name only |

---

## Frontend Code Example (React)

```typescript
const processBatch = async (batchId: number, formData: ProcessBatchForm) => {
  try {
    const response = await fetch('/api/product-detail/process-batch-v2', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        batchId: batchId,
        productGeneralId: formData.productGeneralId,
        price: formData.price,
        storageToolId: formData.storageToolId,
        numOfStar: 0
      })
    });

    const result = await response.json();
    
    if (result.status === 'SUCCESS') {
      const data = result.data;
      
      // Show success message with breakdown
      if (data.verificationType === 'CERTIFICATE') {
        showSuccess(
          `Created ${data.totalProductDetailsCreated} units from ` +
          `Provider ${data.providerId} (Certificate verified)`
        );
      } else {
        // VIDEO batch - show sub-batch breakdown
        const breakdown = data.subBatchBreakdowns
          .map(sb => `Provider ${sb.providerId}: ${sb.productDetailsCreated} units`)
          .join(', ');
        showSuccess(
          `Created ${data.totalProductDetailsCreated} units (Pooled): ${breakdown}`
        );
      }
    } else {
      showError(result.message);
    }
  } catch (error) {
    showError('Failed to process batch');
  }
};
```

---

## Testing Checklist

- [ ] Test CERTIFICATE batch processing (single provider)
- [ ] Test VIDEO batch processing (multiple sub-batches)
- [ ] Test error: Already processed batch
- [ ] Test error: Expired batch
- [ ] Test error: Category mismatch
- [ ] Test error: VIDEO batch without sub-batches
- [ ] Verify provider attribution in ecommerce for CERTIFICATE
- [ ] Verify no attribution for VIDEO batches
- [ ] Verify proportional distribution for VIDEO batches
- [ ] Check Kafka event payload differences

---

## Notes for Developers

1. **Automatic Detection**: The endpoint checks `productBatch.verificationType` to determine workflow
2. **Proportional Distribution**: For VIDEO batches, each sub-batch is processed independently
3. **Event Publishing**: CERTIFICATE batches include provider info in events, VIDEO batches do not
4. **Database References**: 
   - CERTIFICATE: `sub_batch_id = null`
   - VIDEO: `sub_batch_id` set to source sub-batch
5. **Transaction Safety**: All operations are wrapped in `@Transactional`

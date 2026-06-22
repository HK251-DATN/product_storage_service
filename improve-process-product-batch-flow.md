# Improvement Guide: Process Product Batch Flow (V2)

## 📋 Current Implementation Analysis

### Current File Location
`frontend/back-office-ui/src/layout/manage-warehouse/components/ProductDetailCreateModal.jsx`

### Current Flow (Using Legacy Endpoint)
```
1. User selects ProductBatch from dropdown
   ↓
2. System fetches suitable ProductGenerals for batch
   ↓
3. User selects ProductGeneral
   ↓
4. System shows calculation preview (estimate # of products)
   ↓
5. User enters: Price, StorageTool, Status, NumOfStar
   ↓
6. POST to /api/product-detail/process-batch (OLD)
   ↓
7. Success: Shows count of created products
```

### Current Issues
❌ **No visibility of verification type** - User can't tell if it's CERTIFICATE or VIDEO batch  
❌ **No provider attribution info** - Can't see which provider the batch is from  
❌ **No sub-batch breakdown** - For VIDEO batches, no info about individual providers  
❌ **Generic success message** - Just shows count, no breakdown  
❌ **Uses legacy endpoint** - Missing new features of V2  

---

## 🎯 Recommended Improvements

### Phase 1: Update Service Layer (Quick Win)

**File**: `frontend/back-office-ui/src/services/productBatchService.js`

**Add new method**:
```javascript
// Add to existing file
export const processBatchV2 = (data) => 
  axios.post(`${API_URLS.STORAGE}/api/product-detail/process-batch-v2`, data);
```

---

### Phase 2: Enhance ProductDetailCreateModal

**File**: `frontend/back-office-ui/src/layout/manage-warehouse/components/ProductDetailCreateModal.jsx`

#### 2.1 Add New State Variables

```javascript
// Add after existing state declarations (line ~35)
const [verificationType, setVerificationType] = useState(null);
const [providerId, setProviderId] = useState(null);
const [subBatches, setSubBatches] = useState([]);
const [processingResult, setProcessingResult] = useState(null);
```

#### 2.2 Enhance Batch Selection Handler

**Current** (line 99-120):
```javascript
const handleBatchChange = async (batchId) => {
    const batch = batches.find(b => b.batchId === batchId);
    setSelectedBatch(batch);
    // ... fetch suitable products
};
```

**Updated**:
```javascript
const handleBatchChange = async (batchId) => {
    const batch = batches.find(b => b.batchId === batchId);
    setSelectedBatch(batch);
    setSuitableProducts([]);
    setSelectedProduct(null);
    setCalculationPreview('');
    setProcessingResult(null);
    form.setFieldValue('prodGenId', undefined);
    
    // NEW: Set verification type and provider info
    setVerificationType(batch?.verificationType || null);
    setProviderId(batch?.providerId || null);
    
    // NEW: If VIDEO batch, fetch sub-batches
    if (batch?.verificationType === 'VIDEO') {
        try {
            const subBatchResponse = await axios.get(
                `${API_URLS.STORAGE}/api/product-sub-batch/by-batch/${batchId}`
            );
            if (subBatchResponse.data.type === 'GOOD') {
                setSubBatches(subBatchResponse.data.detail || []);
            }
        } catch (error) {
            console.error('Failed to fetch sub-batches:', error);
            setSubBatches([]);
        }
    } else {
        setSubBatches([]);
    }

    if (!batchId) return;

    // Fetch suitable products (existing code)
    try {
        const response = await axios.get(
            `${API_URLS.STORAGE}/api/product-general/suitable-for-batch/${batchId}`
        );
        if (response.data.type === 'GOOD') {
            setSuitableProducts(response.data.detail);
        }
    } catch (error) {
        message.error('Không thể tải danh sách sản phẩm phù hợp');
    }
};
```

#### 2.3 Update Submit Handler to Use V2 Endpoint

**Current** (line 155-188):
```javascript
const handleFinish = async (values) => {
    setLoading(true);
    try {
        const payload = {
            status: values.status,
            price: values.price,
            numOfStar: values.numOfStar || 0,
            storageToolId: values.storageToolId,
            batchId: values.batchId,
            prodGenId: values.prodGenId,
        };
        
        const response = await axios.post(
            `${API_URLS.STORAGE}/api/product-detail/process-batch`, 
            payload
        );
        // ...
    }
};
```

**Updated**:
```javascript
const handleFinish = async (values) => {
    setLoading(true);
    
    try {
        // NEW: Use V2 endpoint with simpler payload
        const payload = {
            batchId: values.batchId,
            productGeneralId: values.prodGenId,
            price: values.price,
            storageToolId: values.storageToolId,
            numOfStar: values.numOfStar || 0,
        };
        
        const response = await axios.post(
            `${API_URLS.STORAGE}/api/product-detail/process-batch-v2`, 
            payload
        );
        
        if (response.data.type === 'GOOD') {
            const result = response.data.detail;
            setProcessingResult(result); // Store for display
            
            // Show detailed success message
            if (result.verificationType === 'CERTIFICATE') {
                message.success({
                    content: (
                        <div>
                            <div><strong>Xử lý batch thành công!</strong></div>
                            <div>Loại: Chứng nhận (Provider #{result.providerId})</div>
                            <div>Đã tạo: {result.totalProductDetailsCreated} sản phẩm</div>
                        </div>
                    ),
                    duration: 5,
                });
            } else {
                // VIDEO batch
                const breakdown = result.subBatchBreakdowns
                    .map(sb => `Provider #${sb.providerId}: ${sb.productDetailsCreated} sản phẩm`)
                    .join(', ');
                    
                message.success({
                    content: (
                        <div>
                            <div><strong>Xử lý batch thành công!</strong></div>
                            <div>Loại: Video (Gộp chung)</div>
                            <div>Tổng: {result.totalProductDetailsCreated} sản phẩm</div>
                            <div style={{ fontSize: '12px', marginTop: 4 }}>
                                Chi tiết: {breakdown}
                            </div>
                        </div>
                    ),
                    duration: 8,
                });
            }
            
            // Don't close modal immediately - show result first
            onSuccess?.();
            
            // Reset after 2 seconds
            setTimeout(() => {
                form.resetFields();
                setSelectedBatch(null);
                setSuitableProducts([]);
                setSelectedProduct(null);
                setCalculationPreview('');
                setVerificationType(null);
                setProviderId(null);
                setSubBatches([]);
                setProcessingResult(null);
                onClose();
            }, 2000);
            
        } else {
            message.error(response.data.message || 'Xử lý batch thất bại!');
        }
    } catch (error) {
        message.error(error.response?.data?.message || 'Có lỗi xảy ra!');
    } finally {
        setLoading(false);
    }
};
```

#### 2.4 Add Verification Type Display

**Insert after batch info Alert** (after line 237):

```jsx
{/* NEW: Verification Type Badge */}
{selectedBatch && verificationType && (
    <Alert
        message={
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                {verificationType === 'CERTIFICATE' ? (
                    <>
                        <Tag color="green">✓ Chứng nhận</Tag>
                        <span>
                            Batch này từ nhà cung cấp đã được chứng nhận 
                            {providerId && ` (Provider #${providerId})`}
                        </span>
                    </>
                ) : (
                    <>
                        <Tag color="blue">📹 Video</Tag>
                        <span>
                            Batch gộp từ nhiều nhà cung cấp ({subBatches.length} sub-batches)
                        </span>
                    </>
                )}
            </div>
        }
        type={verificationType === 'CERTIFICATE' ? 'success' : 'info'}
        style={{ marginBottom: 16 }}
    />
)}

{/* NEW: Sub-batch breakdown for VIDEO batches */}
{verificationType === 'VIDEO' && subBatches.length > 0 && (
    <Alert
        message="Chi tiết Sub-batches"
        description={
            <div style={{ marginTop: 8 }}>
                {subBatches.map((sb, idx) => (
                    <div key={sb.subBatchId} style={{ marginBottom: 4 }}>
                        <Tag>#{idx + 1}</Tag>
                        <strong>Provider #{sb.providerId}</strong>: {sb.quantity}{' '}
                        {UNIT_LABELS[sb.unit]} 
                        <Tag color={sb.processStatus === 'PENDING' ? 'orange' : 'green'} 
                             style={{ marginLeft: 8 }}>
                            {sb.processStatus}
                        </Tag>
                    </div>
                ))}
            </div>
        }
        type="info"
        style={{ marginBottom: 16 }}
    />
)}
```

#### 2.5 Add Processing Result Display

**Insert at the bottom, before footer buttons** (after calculation preview):

```jsx
{/* NEW: Processing Result Display */}
{processingResult && (
    <>
        <Divider>Kết quả xử lý</Divider>
        
        <Alert
            message={
                <div>
                    <div style={{ fontSize: 16, fontWeight: 'bold', marginBottom: 8 }}>
                        ✅ Đã tạo {processingResult.totalProductDetailsCreated} sản phẩm
                    </div>
                    {processingResult.verificationType === 'CERTIFICATE' ? (
                        <div>
                            <Tag color="green">Chứng nhận</Tag>
                            Sản phẩm sẽ hiển thị với tên nhà cung cấp #
                            {processingResult.providerId} trên website
                        </div>
                    ) : (
                        <div>
                            <Tag color="blue">Video (Gộp chung)</Tag>
                            Sản phẩm sẽ hiển thị không có tên nhà cung cấp
                        </div>
                    )}
                </div>
            }
            type="success"
            style={{ marginTop: 16, marginBottom: 16 }}
        />
        
        {processingResult.subBatchBreakdowns?.length > 0 && (
            <div style={{ 
                background: '#f5f5f5', 
                padding: 12, 
                borderRadius: 4,
                marginBottom: 16 
            }}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>
                    Phân bổ theo nhà cung cấp:
                </div>
                {processingResult.subBatchBreakdowns.map((sb, idx) => (
                    <div key={idx} style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between',
                        padding: '4px 0',
                        borderBottom: idx < processingResult.subBatchBreakdowns.length - 1 
                            ? '1px solid #d9d9d9' 
                            : 'none'
                    }}>
                        <span>
                            <Tag>#{idx + 1}</Tag>
                            Provider #{sb.providerId}
                        </span>
                        <span>
                            <strong>{sb.productDetailsCreated}</strong> sản phẩm 
                            ({sb.quantityProcessed} {UNIT_LABELS[selectedBatch?.unit]})
                        </span>
                    </div>
                ))}
            </div>
        )}
    </>
)}
```

---

## 🎨 Enhanced UI/UX Mockup

### Before (Current):
```
┌─────────────────────────────────────────────┐
│ Xử lý Product Batch                    [X]  │
├─────────────────────────────────────────────┤
│ Product Batch: [Dropdown]                   │
│                                              │
│ ℹ️ Thông tin Batch                          │
│ Số lượng: 30 Kg                             │
│ Ngày nhận: 29/04/2026                       │
│                                              │
│ Product General: [Dropdown]                 │
│                                              │
│ ✅ Dự tính: 30 sản phẩm                     │
│                                              │
│ Giá: [50000]                                │
│ Storage Tool: [Dropdown]                    │
│ Trạng thái: [STORED]                        │
│                                              │
│                    [Hủy] [Xử lý Batch]      │
└─────────────────────────────────────────────┘
```

### After (Improved):
```
┌─────────────────────────────────────────────┐
│ Xử lý Product Batch                    [X]  │
├─────────────────────────────────────────────┤
│ Product Batch: [Batch #102 - 30kg Thịt Heo]│
│                                              │
│ ℹ️ Thông tin Batch                          │
│ Số lượng: 30 Kg                             │
│ Ngày nhận: 29/04/2026                       │
│ Hết hạn: 06/05/2026                         │
│                                              │
│ ℹ️ [📹 Video] Batch gộp từ nhiều NCC (3)    │ ← NEW!
│                                              │
│ ℹ️ Chi tiết Sub-batches                     │ ← NEW!
│ #1 Provider #20: 10 Kg [PENDING]           │
│ #2 Provider #30: 10 Kg [PENDING]           │
│ #3 Provider #40: 10 Kg [PENDING]           │
│                                              │
│ Product General: [Thịt Heo - 1kg pack]     │
│                                              │
│ ✅ Dự tính: 30 sản phẩm                     │
│                                              │
│ ─────────────────────────────────────────   │
│ Giá: [50,000] VNĐ                           │
│ Storage Tool: [Fridge #4 - 45%]            │
│ Trạng thái: [STORED]  Sao: [0]             │
│                                              │
│                    [Hủy] [Xử lý Batch]      │
└─────────────────────────────────────────────┘

↓ After Processing ↓

┌─────────────────────────────────────────────┐
│ Xử lý Product Batch                    [X]  │
├─────────────────────────────────────────────┤
│ ... (same form) ...                         │
│                                              │
│ ─────────── Kết quả xử lý ────────────     │ ← NEW!
│                                              │
│ ✅ Đã tạo 30 sản phẩm                       │
│ [📹 Video (Gộp chung)]                      │
│ Sản phẩm sẽ hiển thị không có tên NCC      │
│                                              │
│ Phân bổ theo nhà cung cấp:                  │
│ #1 Provider #20  →  10 sản phẩm (10 Kg)    │
│ #2 Provider #30  →  10 sản phẩm (10 Kg)    │
│ #3 Provider #40  →  10 sản phẩm (10 Kg)    │
│                                              │
│                    [Đóng] (auto-closes)      │
└─────────────────────────────────────────────┘
```

---

## 📝 Implementation Checklist

### Phase 1: Service Layer (5 min)
- [ ] Add `processBatchV2` method to `productBatchService.js`
- [ ] Test API connection to `/api/product-detail/process-batch-v2`

### Phase 2: Component Updates (30 min)
- [ ] Add new state variables (`verificationType`, `providerId`, `subBatches`, `processingResult`)
- [ ] Update `handleBatchChange` to fetch verification info and sub-batches
- [ ] Update `handleFinish` to use V2 endpoint
- [ ] Add verification type badge/alert display
- [ ] Add sub-batch breakdown display (VIDEO batches)
- [ ] Add processing result display panel
- [ ] Update success messages with detailed breakdown

### Phase 3: Styling & Polish (15 min)
- [ ] Add icons (✓ for CERTIFICATE, 📹 for VIDEO)
- [ ] Color-code verification types (green for CERTIFICATE, blue for VIDEO)
- [ ] Format numbers with thousand separators
- [ ] Add loading states for sub-batch fetching
- [ ] Test responsive layout

### Phase 4: Testing (20 min)
- [ ] Test CERTIFICATE batch processing (single provider)
- [ ] Test VIDEO batch processing (multiple providers)
- [ ] Test error handling (expired batch, category mismatch, etc.)
- [ ] Verify result display accuracy
- [ ] Check auto-close timing
- [ ] Verify refresh triggers work correctly

---

## 🔄 Comparison: Old vs New

| Aspect | Old (V1) | New (V2) |
|--------|----------|----------|
| **Endpoint** | `/process-batch` | `/process-batch-v2` ✅ |
| **Request Fields** | 6 fields (status, price, numOfStar, storageToolId, batchId, prodGenId) | 5 fields (simpler) ✅ |
| **Verification Visibility** | ❌ None | ✅ Shows CERTIFICATE/VIDEO badge |
| **Provider Attribution** | ❌ Hidden | ✅ Displays for CERTIFICATE |
| **Sub-batch Info** | ❌ Not shown | ✅ Lists all sub-batches with details |
| **Result Display** | Just count | ✅ Full breakdown by provider |
| **Success Message** | Generic | ✅ Detailed with attribution info |
| **Event Data** | Basic | ✅ Includes provider info for CERTIFICATE |

---

## 🚀 Benefits of V2 Implementation

### For Users (Warehouse Staff)
✅ **Clear visibility** of batch verification type  
✅ **Know which provider** products come from (CERTIFICATE)  
✅ **See distribution** across multiple providers (VIDEO)  
✅ **Better transparency** in the processing workflow  
✅ **Detailed feedback** on what was created  

### For Business
✅ **Marketing advantage** - Can promote certified products  
✅ **Quality tracking** - Know which batches came from which providers  
✅ **Compliance** - Audit trail for certified products  
✅ **Customer trust** - Show provider attribution on ecommerce  

### For Developers
✅ **Single endpoint** handles both workflows  
✅ **Automatic detection** of batch type  
✅ **Better error handling** at API level  
✅ **Clearer response structure** for UI rendering  

---

## 🐛 Common Issues & Solutions

### Issue 1: Sub-batches not loading
**Symptom**: VIDEO batch shows "0 sub-batches"  
**Solution**: Check that sub-batches have `product_batch_id` correctly set and are in PENDING status

### Issue 2: Processing fails with "already processed"
**Symptom**: Error message "Product batch is already PROCESSED"  
**Solution**: Filter out batches with `processStatus !== 'PENDING'` in batch selection dropdown

### Issue 3: Wrong provider attribution
**Symptom**: CERTIFICATE batch shows `providerId: null`  
**Solution**: Ensure `providerId` is set when creating CERTIFICATE batches

---

## 📚 Additional Resources

- **API Documentation**: `/services/product_storage_service/BATCH_PROCESSING_API.md`
- **Backend Implementation**: `ProductDetailServiceImpl.java` - `processProductBatchV2()` method
- **Database Schema**: `/services/product_storage_service/db_scheme/README.md`

---

## 🎯 Next Steps After Implementation

1. **Add filters** to batch selection dropdown:
   - Filter by verification type (CERTIFICATE/VIDEO)
   - Filter by status (PENDING only for processing)
   - Filter by expiry date (show expiring soon first)

2. **Enhance calculation preview**:
   - For VIDEO batches, show per-provider calculation
   - Add warning if batch expires soon

3. **Add batch comparison**:
   - Compare prices across similar batches
   - Show historical processing data

4. **Provider integration**:
   - Click provider ID to view provider details
   - Show provider rating/history

---

## 💡 Tips for Frontend Developers

1. **Import the new service method**:
   ```javascript
   import { processBatchV2 } from '../../../services/productBatchService';
   ```

2. **Handle loading states**:
   ```javascript
   const [loadingSubBatches, setLoadingSubBatches] = useState(false);
   ```

3. **Add TypeScript types** (if using TS):
   ```typescript
   interface ProcessBatchResult {
     verificationType: 'CERTIFICATE' | 'VIDEO';
     totalProductDetailsCreated: number;
     batchId: number;
     productGeneralId: number;
     providerId?: number | null;
     subBatchBreakdowns?: SubBatchBreakdown[];
   }
   
   interface SubBatchBreakdown {
     subBatchId: number;
     providerId: number;
     quantityProcessed: number;
     productDetailsCreated: number;
   }
   ```

4. **Use constants for verification types**:
   ```javascript
   const VERIFICATION_TYPES = {
     CERTIFICATE: 'CERTIFICATE',
     VIDEO: 'VIDEO'
   };
   ```

---

**Last Updated**: 2026-04-29  
**Version**: 2.0  
**Author**: System Documentation

package edu.hcmut.datn.productstorage.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import edu.hcmut.datn.productstorage.common.enums.TransactionStatus;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import edu.hcmut.datn.productstorage.dto.request.RawProductDemandConfirmRequest;
import edu.hcmut.datn.productstorage.dto.request.RawProductDemandCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.RawProductDemandUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.dto.response.PagedResponse;
import edu.hcmut.datn.productstorage.dto.response.TransactionHistoryResponse;
import edu.hcmut.datn.productstorage.security.portable.AuthenticatedUser;
import edu.hcmut.datn.productstorage.service.RawProductDemandService;
import lombok.AllArgsConstructor;

/**
 * REST controller for Raw Product Demand management.
 *
 * =========================================================
 * FRONTEND GUIDE — Transaction History Endpoints
 * =========================================================
 *
 * Two endpoints are available:
 *
 *   GET /api/raw-product-demand/transaction-history
 *     → Admin use. Accepts an optional ?providerId= to scope results.
 *
 *   GET /api/raw-product-demand/transaction-history/me
 *     → Provider use. providerId is read from the JWT token automatically.
 *       Do NOT pass providerId — it is ignored on this endpoint.
 *
 * ---------------------------------------------------------
 * QUERY PARAMETERS (all optional)
 * ---------------------------------------------------------
 *   status           WAIT_FOR_DELIVERY | FINISHED | REJECTED | EXPIRED
 *   subSubcategoryId  Filter by product category ID
 *   demandId         Filter by a specific demand ID
 *   dateNeedFrom     Demand date range start, format: YYYY-MM-DD (inclusive)
 *   dateNeedTo       Demand date range end,   format: YYYY-MM-DD (inclusive)
 *   sortBy           createdAt (default) | dateNeed | quantity | unitPrice | receivedAt
 *   sortDir          DESC (default) | ASC
 *   pageNum          1-based page index (default: 1)
 *   pageSize         Records per page   (default: 20)
 *
 * ---------------------------------------------------------
 * EXAMPLE REQUESTS
 * ---------------------------------------------------------
 *   // Provider: own history, latest first
 *   GET /api/raw-product-demand/transaction-history/me
 *   Authorization: Bearer <token>
 *
 *   // Provider: filter by status and date range
 *   GET /api/raw-product-demand/transaction-history/me
 *       ?status=FINISHED&dateNeedFrom=2026-05-01&dateNeedTo=2026-05-31&pageSize=10
 *   Authorization: Bearer <token>
 *
 *   // Admin: all transactions for provider 42, sorted by quantity ascending
 *   GET /api/raw-product-demand/transaction-history?providerId=42&sortBy=quantity&sortDir=ASC
 *
 * ---------------------------------------------------------
 * RESPONSE SHAPE  (detail field of ApiResponse)
 * ---------------------------------------------------------
 *   {
 *     "data": [
 *       {
 *         "id":                7,
 *         "batchType":         "CERTIFICATE",   // CERTIFICATE | VIDEO
 *         "demandId":          3,
 *         "subSubcategoryId":  12,
 *         "subSubcategoryName":"Thịt Heo",
 *         "quantity":          20,
 *         "unit":              "KILOGRAM",
 *         "dateNeed":          "2026-05-20",
 *         "unitPrice":         85000,
 *         "status":            "FINISHED",
 *         "providerId":        42,
 *         "receivedAt":        "2026-05-18T09:30:00",
 *         "createdAt":         "2026-05-15T14:00:00"
 *       }
 *     ],
 *     "page": 1, "size": 20, "totalElements": 1, "totalPages": 1
 *   }
 *
 * ---------------------------------------------------------
 * STATUS MEANINGS
 * ---------------------------------------------------------
 *   WAIT_FOR_DELIVERY  Provider confirmed the demand; delivery not yet received
 *   FINISHED           Delivery received (batch pending processing or already processed)
 *   REJECTED           Delivery rejected by warehouse staff
 *   EXPIRED            Batch passed its expiry date before being processed
 *
 * batchType note:
 *   CERTIFICATE → "id" refers to a ProductBatch ID   (single certified provider)
 *   VIDEO       → "id" refers to a ProductSubBatch ID (pooled, multiple providers)
 * =========================================================
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/raw-product-demand")
public class RawProductDemandController {

    private final RawProductDemandService rawProductDemandService;

    @PostMapping
    public ResponseEntity<ApiResponse<RawProductDemand>> create(@RequestBody RawProductDemandCreateRequest request) {
        try {
            RawProductDemand created = rawProductDemandService.create(request.toEntity());
            return ResponseEntity.ok(ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Raw product demand created", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{demandId}")
    public ResponseEntity<ApiResponse<RawProductDemand>> read(@PathVariable Long demandId) {
        try {
            RawProductDemand demand = rawProductDemandService.read(demandId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demand retrieved", demand));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RawProductDemand>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<RawProductDemand> demands = rawProductDemandService.readAll(pageNum, pageSize);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demands retrieved", demands));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @PutMapping("/{demandId}")
    public ResponseEntity<ApiResponse<RawProductDemand>> update(
            @PathVariable Long demandId,
            @RequestBody RawProductDemandUpdateRequest request) {
        try {
            RawProductDemand existing = rawProductDemandService.read(demandId);
            existing.setSubSubcategoryId(request.getSubSubcategoryId());
            existing.setUnit(request.getUnit());
            existing.setUnitQuantity(request.getUnitQuantity());
            existing.setUnitPrice(request.getUnitPrice());
            existing.setDateNeed(request.getDateNeed());
            existing.setNote(request.getNote());
            existing.setStatus(request.getStatus());
            RawProductDemand updated = rawProductDemandService.update(demandId, existing);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demand updated", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{demandId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long demandId) {
        try {
            rawProductDemandService.delete(demandId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demand deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/by-category/{subSubcategoryId}")
    public ResponseEntity<ApiResponse<List<RawProductDemand>>> findBySubSubcategoryId(@PathVariable Long subSubcategoryId) {
        try {
            List<RawProductDemand> demands = rawProductDemandService.findBySubSubcategoryId(subSubcategoryId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Demands retrieved by category", demands));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/{demandId}/confirm")
    public ResponseEntity<ApiResponse<RawProductDemand>> confirm(
            @PathVariable Long demandId,
            @RequestBody RawProductDemandConfirmRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            Long providerId = principal.getId();
            RawProductDemand updated = rawProductDemandService.confirmForProvider(
                    demandId, providerId, request.getQuantity(), request.getNote());
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Demand confirmed by provider", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    /**
     * Retrieves paginated transaction history between the store and providers.
     *
     * <p>A transaction represents a product delivery commitment made by a provider in response to a
     * {@code RawProductDemand}. Each row in the result corresponds to either:
     * <ul>
     *   <li>A {@code ProductBatch} with {@code verificationType = CERTIFICATE} — one certified
     *       provider owns the entire batch.</li>
     *   <li>A {@code ProductSubBatch} with {@code verificationType = VIDEO} — one provider's
     *       individual delivery into a shared pooled batch.</li>
     * </ul>
     *
     * <p><b>Transaction status mapping:</b>
     * <ul>
     *   <li>{@code WAIT_FOR_DELIVERY} — provider confirmed the demand but has not yet delivered.</li>
     *   <li>{@code FINISHED} — delivery received; batch is pending processing or already processed.</li>
     *   <li>{@code REJECTED} — delivery was rejected by warehouse staff.</li>
     *   <li>{@code EXPIRED} — batch passed its expiry date before being processed.</li>
     * </ul>
     *
     * <p><b>Filter parameters</b> (all optional):
     * <ul>
     *   <li>{@code providerId} — show only transactions for the given provider.</li>
     *   <li>{@code status} — one of {@code WAIT_FOR_DELIVERY}, {@code FINISHED}, {@code REJECTED},
     *       {@code EXPIRED}.</li>
     *   <li>{@code subSubcategoryId} — show only transactions for the given product category.</li>
     *   <li>{@code demandId} — show only transactions linked to the given demand.</li>
     *   <li>{@code dateNeedFrom} / {@code dateNeedTo} — filter by the demand's required delivery date
     *       (ISO format {@code YYYY-MM-DD}, inclusive on both ends).</li>
     * </ul>
     *
     * <p><b>Sort parameters:</b>
     * <ul>
     *   <li>{@code sortBy} — field to sort on; accepted values: {@code createdAt} (default),
     *       {@code dateNeed}, {@code quantity}, {@code unitPrice}, {@code receivedAt}.</li>
     *   <li>{@code sortDir} — {@code DESC} (default) or {@code ASC}.</li>
     * </ul>
     *
     * @param providerId      (optional) filter by provider ID
     * @param status          (optional) filter by transaction status
     * @param subSubcategoryId (optional) filter by sub-subcategory ID
     * @param demandId        (optional) filter by raw product demand ID
     * @param dateNeedFrom    (optional) earliest dateNeed to include (ISO date)
     * @param dateNeedTo      (optional) latest dateNeed to include (ISO date)
     * @param sortBy          field to sort by (default: {@code createdAt})
     * @param sortDir         sort direction — {@code ASC} or {@code DESC} (default: {@code DESC})
     * @param pageNum         1-based page number (default: 1)
     * @param pageSize        number of records per page (default: 20)
     * @return paginated list of {@link TransactionHistoryResponse}
     */
    @GetMapping("/transaction-history")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionHistoryResponse>>> getTransactionHistory(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) Long subSubcategoryId,
            @RequestParam(required = false) Long demandId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNeedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNeedTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            PagedResponse<TransactionHistoryResponse> result = rawProductDemandService.getTransactionHistory(
                    providerId, status, subSubcategoryId, demandId,
                    dateNeedFrom, dateNeedTo, sortBy, sortDir, pageNum, pageSize);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Transaction history retrieved", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    /**
     * Retrieves the transaction history for the currently authenticated provider.
     *
     * <p>Identical to {@code GET /transaction-history} but the {@code providerId} is sourced
     * from the JWT token, so a provider can only access their own transactions.
     *
     * @see #getTransactionHistory for full parameter and status documentation
     *
     * @param status          (optional) filter by transaction status
     * @param subSubcategoryId (optional) filter by sub-subcategory ID
     * @param demandId        (optional) filter by raw product demand ID
     * @param dateNeedFrom    (optional) earliest dateNeed to include (ISO date)
     * @param dateNeedTo      (optional) latest dateNeed to include (ISO date)
     * @param sortBy          field to sort by (default: {@code createdAt})
     * @param sortDir         sort direction — {@code ASC} or {@code DESC} (default: {@code DESC})
     * @param pageNum         1-based page number (default: 1)
     * @param pageSize        number of records per page (default: 20)
     * @param principal       the authenticated provider, injected from the JWT token
     * @return paginated list of {@link TransactionHistoryResponse} scoped to the caller
     */
    @GetMapping("/transaction-history/me")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionHistoryResponse>>> getMyTransactionHistory(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) Long subSubcategoryId,
            @RequestParam(required = false) Long demandId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNeedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNeedTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return getTransactionHistory(
                principal.getId(), status, subSubcategoryId, demandId,
                dateNeedFrom, dateNeedTo, sortBy, sortDir, pageNum, pageSize);
    }
}

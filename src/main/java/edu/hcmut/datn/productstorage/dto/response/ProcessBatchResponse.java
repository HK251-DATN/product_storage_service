package edu.hcmut.datn.productstorage.dto.response;

import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for batch processing showing detailed breakdown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessBatchResponse {

    /**
     * Batch verification type (CERTIFICATE or VIDEO)
     */
    private ProviderVerificationType verificationType;

    /**
     * Total number of product details created
     */
    private Long totalProductDetailsCreated;

    /**
     * Batch ID
     */
    private Long batchId;

    /**
     * Product General ID
     */
    private Long productGeneralId;

    /**
     * For CERTIFICATE batches: the single provider ID
     * For VIDEO batches: null
     */
    private Long providerId;

    /**
     * For VIDEO batches: breakdown by sub-batch
     * For CERTIFICATE batches: empty list
     */
    private List<SubBatchBreakdown> subBatchBreakdowns;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubBatchBreakdown {
        private Long subBatchId;
        private Long providerId;
        private Long quantityProcessed;
        private Long productDetailsCreated;
    }
}

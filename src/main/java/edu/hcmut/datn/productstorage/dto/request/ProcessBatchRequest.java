package edu.hcmut.datn.productstorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple request for processing a batch into product details.
 * Works for both CERTIFICATE and VIDEO verified batches automatically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessBatchRequest {

    /**
     * The ProductBatch ID to process
     */
    private Long batchId;

    /**
     * The ProductGeneral ID for the product type
     */
    private Long productGeneralId;

    /**
     * Price per product detail unit
     */
    private Long price;

    /**
     * Storage tool ID where products will be stored
     */
    private Long storageToolId;

    /**
     * Initial star rating (optional, defaults to 0)
     */
    private Long numOfStar;
}

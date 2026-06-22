package edu.hcmut.datn.productstorage.messaging.batchdetail;

public record BatchDetailCreateEvent (
        Long batchDetailId,
        Long productGeneralId,
        Long quantity,
        Long price,
        Long avgRate,
        Long numRate,
        String detailContent,
        Long subBatchId,
        String verificationType,  // CERTIFICATE or VIDEO
        String certificateType,   // VIETGAP, GLOBALGAP, etc. (null for VIDEO)
        Long providerId,
        String logoUrl  // Provider logo URL (null for VIDEO)
) {
}

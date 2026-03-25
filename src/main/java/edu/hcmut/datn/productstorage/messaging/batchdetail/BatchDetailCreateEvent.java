package edu.hcmut.datn.productstorage.messaging.batchdetail;

public record BatchDetailCreateEvent (
        Long batchDetailId,
        Long productGeneralId,
        Long quantity,
        Long price,
        Long avgRate,
        Long numRate,
        String detailContent
) {
}

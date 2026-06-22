package edu.hcmut.datn.productstorage.dto.response;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AvailableProductResponse {

    // ProductBatch fields
    private Long batchId;
    private Long batchQuantity;
    private Unit batchUnit;
    private LocalDateTime receivedAt;
    private LocalDateTime expiredAt;
    private ProductBatchProcessStatus processStatus;
    private ProviderVerificationType verificationType;

    // ProductGeneral fields
    private Long prodGenId;
    private String name;
    private String img;
    private Unit unit;
    private Long unitQuantity;

    public AvailableProductResponse(ProductBatch batch, ProductGeneral productGeneral) {
        this.batchId = batch.getBatchId();
        this.batchQuantity = batch.getQuantity();
        this.batchUnit = batch.getUnit();
        this.receivedAt = batch.getReceivedAt();
        this.expiredAt = batch.getExpiredAt();
        this.processStatus = batch.getProcessStatus();
        this.verificationType = batch.getVerificationType();

        if (productGeneral != null) {
            this.prodGenId = productGeneral.getProdGenId();
            this.name = productGeneral.getName();
            this.img = productGeneral.getImgUrl();
            this.unit = productGeneral.getUnit();
            this.unitQuantity = productGeneral.getUnitQuantity();
        }
    }
}

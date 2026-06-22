package edu.hcmut.datn.productstorage.dto.response;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.Provider;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import lombok.Getter;

@Getter
public class ProductBatchDetailResponse {
    private final ProductBatch batch;
    // CERTIFICATE: single provider attribution
    private final ProviderInfo provider;
    // VIDEO: list of sub-batches, each with their own provider
    private final List<ProductSubBatchResponse> subBatches;

    public static ProductBatchDetailResponse certificate(ProductBatch batch, Provider provider) {
        return new ProductBatchDetailResponse(batch,
                provider != null ? new ProviderInfo(provider.getProviderId(), provider.getFName(), provider.getLName(), provider.getAvtUrl()) : null,
                null);
    }

    public static ProductBatchDetailResponse video(ProductBatch batch, List<ProductSubBatchResponse> subBatches) {
        return new ProductBatchDetailResponse(batch, null, subBatches);
    }

    private ProductBatchDetailResponse(ProductBatch batch, ProviderInfo provider, List<ProductSubBatchResponse> subBatches) {
        this.batch = batch;
        this.provider = provider;
        this.subBatches = subBatches;
    }
}

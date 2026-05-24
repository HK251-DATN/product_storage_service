package edu.hcmut.datn.productstorage.dto.response;

import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import edu.hcmut.datn.productstorage.dao.Provider;
import lombok.Getter;

@Getter
public class ProductSubBatchResponse {
    private final ProductSubBatch subBatch;
    private final ProviderInfo provider;

    public ProductSubBatchResponse(ProductSubBatch subBatch, Provider provider) {
        this.subBatch = subBatch;
        this.provider = provider != null
                ? new ProviderInfo(provider.getProviderId(), provider.getFName(), provider.getLName(), provider.getAvtUrl())
                : null;
    }
}

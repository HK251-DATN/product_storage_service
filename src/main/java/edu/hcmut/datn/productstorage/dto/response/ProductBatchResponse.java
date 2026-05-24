package edu.hcmut.datn.productstorage.dto.response;

import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.Provider;
import lombok.Getter;

@Getter
public class ProductBatchResponse {
    private final ProductBatch batch;
    private final ProviderInfo provider;

    public ProductBatchResponse(ProductBatch batch, Provider provider) {
        this.batch = batch;
        this.provider = provider != null
                ? new ProviderInfo(provider.getProviderId(), provider.getFName(), provider.getLName(), provider.getAvtUrl())
                : null;
    }
}

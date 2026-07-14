package edu.hcmut.datn.productstorage.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.hcmut.datn.productstorage.common.enums.StorageToolStatus;
import edu.hcmut.datn.productstorage.common.enums.StorageType;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import lombok.Getter;
import lombok.Setter;

/**
 * Binds {@code src/main/resources/init_data.json} for {@link DataSeeder}.
 *
 * SubSubcategory/ProductGeneral rows are not seeded here - they arrive
 * asynchronously via Kafka from back-office-service's own DataSeeder
 * (SubSubcategoryCreatedConsumer / ProductGeneralCreatedConsumer). ProductBatch
 * and RawProductDemand reference a SubSubcategory by name (resolved against the
 * local, Kafka-populated table at seed time) rather than by id, since
 * back-office now assigns SubSubcategory ids via auto-increment instead of the
 * old hardcoded 1-55 scheme this service used to duplicate.
 */
@Getter
@Setter
public class InitData {

    @JsonProperty("Warehouses")
    private List<WarehouseSeed> warehouses;

    @JsonProperty("StorageTools")
    private List<StorageToolSeed> storageTools;

    @JsonProperty("Racks")
    private List<RackSeed> racks;

    @JsonProperty("Fridges")
    private List<FridgeSeed> fridges;

    @JsonProperty("ProductBatches")
    private List<ProductBatchSeed> productBatches;

    @JsonProperty("RawProductDemands")
    private List<RawProductDemandSeed> rawProductDemands;

    @Getter
    @Setter
    public static class WarehouseSeed {
        private int id;
        private String address;
    }

    @Getter
    @Setter
    public static class StorageToolSeed {
        private int id;
        private int warehouseId;
        private StorageType toolType;
        private StorageToolStatus status;
        private int lastMaintainanceMonthsAgo;
    }

    @Getter
    @Setter
    public static class RackSeed {
        private int storageToolId;
        private Long numOfLevel;
    }

    @Getter
    @Setter
    public static class FridgeSeed {
        private int storageToolId;
        private Long curTemp;
        private Long minTemp;
        private Long maxTemp;
    }

    @Getter
    @Setter
    public static class ProductBatchSeed {
        private String subSubcategoryName;
        private Long quantity;
        private Unit unit;
    }

    @Getter
    @Setter
    public static class RawProductDemandSeed {
        private String subSubcategoryName;
        private Unit unit;
        private Long unitQuantity;
        private Long unitPrice;
        private int daysUntilNeeded;
        private String note;
    }
}

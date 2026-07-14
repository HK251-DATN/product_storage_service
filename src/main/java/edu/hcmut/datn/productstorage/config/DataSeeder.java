package edu.hcmut.datn.productstorage.config;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.dao.*;
import edu.hcmut.datn.productstorage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private static final String INIT_DATA_FILE = "init_data.json";

    // ProductBatch/RawProductDemand need SubSubcategory rows that arrive
    // asynchronously via Kafka from back-office-service, in a separate process -
    // there's no ordering guarantee between "this service's own seed data is
    // written" and "its Kafka consumer has caught up". Bound the wait so a fresh
    // `docker compose up` doesn't need a manual restart to seed them correctly.
    private static final Duration SUBSUBCATEGORY_WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration SUBSUBCATEGORY_POLL_INTERVAL = Duration.ofSeconds(1);

    private final WarehouseRepository warehouseRepository;
    private final StorageToolRepository storageToolRepository;
    private final RackRepository rackRepository;
    private final FridgeRepository fridgeRepository;
    private final RackLevelRepository rackLevelRepository;
    private final SubSubcategoryRepository subSubcategoryRepository;
    private final ProductBatchRepository productBatchRepository;
    private final RawProductDemandRepository rawProductDemandRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Only seed if database is empty
            if (warehouseRepository.count() > 0) {
                log.info("Database already contains data. Skipping seeding.");
                return;
            }

            log.info("Starting database seeding from {}...", INIT_DATA_FILE);

            InitData initData;
            try (InputStream is = new ClassPathResource(INIT_DATA_FILE).getInputStream()) {
                initData = objectMapper.readValue(is, InitData.class);
            }

            Map<Integer, Long> warehouseIdByJsonId = seedWarehouses(initData.getWarehouses());
            Map<Integer, Long> storageToolIdByJsonId =
                    seedStorageTools(initData.getStorageTools(), warehouseIdByJsonId);
            seedRacks(initData.getRacks(), storageToolIdByJsonId);
            seedFridges(initData.getFridges(), storageToolIdByJsonId);

            // SubSubcategories/ProductGenerals are not seeded here - they arrive
            // asynchronously via Kafka from back-office-service's own DataSeeder
            // (SubSubcategoryCreatedConsumer / ProductGeneralCreatedConsumer).
            // ProductBatch/RawProductDemand need those rows to already exist locally,
            // so wait (bounded) for them to arrive before seeding.
            waitForSubSubcategories(collectRequiredSubSubcategoryNames(initData));
            seedProductBatches(initData.getProductBatches());
            seedRawProductDemands(initData.getRawProductDemands());

            log.info("Database seeding completed successfully!");
        };
    }

    private Map<Integer, Long> seedWarehouses(List<InitData.WarehouseSeed> warehouses) {
        Map<Integer, Long> warehouseIdByJsonId = new HashMap<>();

        for (InitData.WarehouseSeed seed : warehouses) {
            Warehouse warehouse = new Warehouse(seed.getAddress(), 0L, 0L, 0L);
            Warehouse saved = warehouseRepository.save(warehouse);
            warehouseIdByJsonId.put(seed.getId(), saved.getWarehouseId());
        }

        log.info("Seeded {} warehouses", warehouseRepository.count());
        return warehouseIdByJsonId;
    }

    private Map<Integer, Long> seedStorageTools(List<InitData.StorageToolSeed> storageTools,
                                                 Map<Integer, Long> warehouseIdByJsonId) {
        Map<Integer, Long> storageToolIdByJsonId = new HashMap<>();

        for (InitData.StorageToolSeed seed : storageTools) {
            Long warehouseId = warehouseIdByJsonId.get(seed.getWarehouseId());
            if (warehouseId == null) {
                log.warn("StorageTool references unknown warehouseId={}, skipping", seed.getWarehouseId());
                continue;
            }

            StorageTool storageTool = new StorageTool(
                    LocalDate.now().minusMonths(seed.getLastMaintainanceMonthsAgo()),
                    seed.getStatus(), 0L, warehouseId, seed.getToolType());
            StorageTool saved = storageToolRepository.save(storageTool);
            storageToolIdByJsonId.put(seed.getId(), saved.getStorageToolId());
        }

        log.info("Seeded {} storage tools", storageToolRepository.count());
        return storageToolIdByJsonId;
    }

    private void seedRacks(List<InitData.RackSeed> racks, Map<Integer, Long> storageToolIdByJsonId) {
        for (InitData.RackSeed seed : racks) {
            Long storageToolId = storageToolIdByJsonId.get(seed.getStorageToolId());
            if (storageToolId == null) {
                log.warn("Rack references unknown storageToolId={}, skipping", seed.getStorageToolId());
                continue;
            }

            Rack rack = rackRepository.save(new Rack(seed.getNumOfLevel(), storageToolId));
            for (long i = 0; i < seed.getNumOfLevel(); i++) {
                rackLevelRepository.save(new RackLevel(0L, rack.getRackId()));
            }
        }

        log.info("Seeded {} racks with {} rack levels", rackRepository.count(), rackLevelRepository.count());
    }

    private void seedFridges(List<InitData.FridgeSeed> fridges, Map<Integer, Long> storageToolIdByJsonId) {
        for (InitData.FridgeSeed seed : fridges) {
            Long storageToolId = storageToolIdByJsonId.get(seed.getStorageToolId());
            if (storageToolId == null) {
                log.warn("Fridge references unknown storageToolId={}, skipping", seed.getStorageToolId());
                continue;
            }

            fridgeRepository.save(new Fridge(seed.getCurTemp(), seed.getMinTemp(), seed.getMaxTemp(), storageToolId));
        }

        log.info("Seeded {} fridges", fridgeRepository.count());
    }

    private Set<String> collectRequiredSubSubcategoryNames(InitData initData) {
        Set<String> names = new HashSet<>();
        initData.getProductBatches().forEach(seed -> names.add(seed.getSubSubcategoryName()));
        initData.getRawProductDemands().forEach(seed -> names.add(seed.getSubSubcategoryName()));
        return names;
    }

    private void waitForSubSubcategories(Set<String> requiredNames) {
        Instant deadline = Instant.now().plus(SUBSUBCATEGORY_WAIT_TIMEOUT);
        Set<String> missing = requiredNames;

        log.info("Waiting up to {}s for {} SubSubcategories to arrive via Kafka from back-office-service...",
                SUBSUBCATEGORY_WAIT_TIMEOUT.getSeconds(), missing.size());

        while (!missing.isEmpty() && Instant.now().isBefore(deadline)) {
            Set<String> stillMissing = new HashSet<>();
            for (String name : missing) {
                if (subSubcategoryRepository.findByName(name).isEmpty()) {
                    stillMissing.add(name);
                }
            }
            missing = stillMissing;

            if (!missing.isEmpty()) {
                try {
                    Thread.sleep(SUBSUBCATEGORY_POLL_INTERVAL.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (missing.isEmpty()) {
            log.info("All required SubSubcategories are available.");
        } else {
            log.warn("Timed out waiting for {} SubSubcategories to arrive via Kafka: {}. "
                            + "Corresponding ProductBatch/RawProductDemand rows will be skipped.",
                    missing.size(), missing);
        }
    }

    private void seedProductBatches(List<InitData.ProductBatchSeed> productBatches) {
        int skipped = 0;

        for (InitData.ProductBatchSeed seed : productBatches) {
            Optional<SubSubcategory> subSubcategory = subSubcategoryRepository.findByName(seed.getSubSubcategoryName());
            if (subSubcategory.isEmpty()) {
                log.warn("ProductBatch references SubSubcategory '{}' which hasn't arrived via Kafka yet, skipping",
                        seed.getSubSubcategoryName());
                skipped++;
                continue;
            }

            LocalDateTime receivedAt = LocalDateTime.now();
            LocalDateTime expiredAt = receivedAt.plusDays(subSubcategory.get().getAvgShelfDays());

            ProductBatch batch = new ProductBatch(seed.getQuantity(), seed.getUnit(), "", receivedAt, expiredAt,
                    null, subSubcategory.get().getSubSubcategoryId());
            batch.setVerificationType(ProviderVerificationType.CERTIFICATE);
            batch.setProcessStatus(ProductBatchProcessStatus.PENDING);
            productBatchRepository.save(batch);
        }

        log.info("Seeded {} product batches ({} skipped - SubSubcategory not yet available)",
                productBatchRepository.count(), skipped);
    }

    private void seedRawProductDemands(List<InitData.RawProductDemandSeed> rawProductDemands) {
        int skipped = 0;

        for (InitData.RawProductDemandSeed seed : rawProductDemands) {
            Optional<SubSubcategory> subSubcategory = subSubcategoryRepository.findByName(seed.getSubSubcategoryName());
            if (subSubcategory.isEmpty()) {
                log.warn("RawProductDemand references SubSubcategory '{}' which hasn't arrived via Kafka yet, skipping",
                        seed.getSubSubcategoryName());
                skipped++;
                continue;
            }

            RawProductDemand demand = new RawProductDemand(subSubcategory.get().getSubSubcategoryId(), seed.getUnit(),
                    seed.getUnitQuantity(), seed.getUnitPrice(),
                    LocalDate.now().plusDays(seed.getDaysUntilNeeded()), seed.getNote());
            demand.setStatus(RawProductDemandStatus.PENDING);
            rawProductDemandRepository.save(demand);
        }

        log.info("Seeded {} raw product demands ({} skipped - SubSubcategory not yet available)",
                rawProductDemandRepository.count(), skipped);
    }
}

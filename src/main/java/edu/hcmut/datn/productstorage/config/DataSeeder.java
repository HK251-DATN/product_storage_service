package edu.hcmut.datn.productstorage.config;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.StorageToolStatus;
import edu.hcmut.datn.productstorage.common.enums.StorageType;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.*;
import edu.hcmut.datn.productstorage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final WarehouseRepository warehouseRepository;
    private final StorageToolRepository storageToolRepository;
    private final RackRepository rackRepository;
    private final FridgeRepository fridgeRepository;
    private final RackLevelRepository rackLevelRepository;
    private final SubSubcategoryRepository subSubcategoryRepository;
    private final ProductGeneralRepository productGeneralRepository;
    private final ProductBatchRepository productBatchRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Only seed if database is empty
            if (warehouseRepository.count() > 0) {
                log.info("Database already contains data. Skipping seeding.");
                return;
            }

            log.info("Starting database seeding...");

            // Seed Warehouses
            Warehouse warehouse1 = createWarehouse("123 Nguyễn Văn Linh, Quận 7, TP.HCM", 0L, 0L, 0L);
            Warehouse warehouse2 = createWarehouse("456 Võ Văn Ngân, Thủ Đức, TP.HCM", 0L, 0L, 0L);
            Warehouse warehouse3 = createWarehouse("789 Quang Trung, Gò Vấp, TP.HCM", 0L, 0L, 0L);

            log.info("Seeded {} warehouses", warehouseRepository.count());

            // Seed Storage Tools - Racks for general dry storage
            StorageTool rack1 = createStorageTool(LocalDate.now().minusMonths(3), StorageToolStatus.ACTIVE, 0L,
                warehouse1.getWarehouseId(), StorageType.RACK);
            StorageTool rack2 = createStorageTool(LocalDate.now().minusMonths(2), StorageToolStatus.ACTIVE, 0L,
                warehouse1.getWarehouseId(), StorageType.RACK);
            StorageTool rack3 = createStorageTool(LocalDate.now().minusMonths(1), StorageToolStatus.ACTIVE, 0L,
                warehouse2.getWarehouseId(), StorageType.RACK);
            StorageTool rack4 = createStorageTool(LocalDate.now().minusMonths(4), StorageToolStatus.ACTIVE, 0L,
                warehouse2.getWarehouseId(), StorageType.RACK);

            // Seed Storage Tools - Fridges for fresh products
            StorageTool fridge1 = createStorageTool(LocalDate.now().minusMonths(6), StorageToolStatus.ACTIVE, 0L,
                warehouse1.getWarehouseId(), StorageType.FRIDGE);
            StorageTool fridge2 = createStorageTool(LocalDate.now().minusMonths(5), StorageToolStatus.ACTIVE, 0L,
                warehouse2.getWarehouseId(), StorageType.FRIDGE);
            StorageTool fridge3 = createStorageTool(LocalDate.now().minusMonths(2), StorageToolStatus.ACTIVE, 0L,
                warehouse3.getWarehouseId(), StorageType.FRIDGE);
            StorageTool fridge4 = createStorageTool(LocalDate.now().minusMonths(3), StorageToolStatus.ACTIVE, 0L,
                warehouse3.getWarehouseId(), StorageType.FRIDGE);

            log.info("Seeded {} storage tools", storageToolRepository.count());

            // Seed Racks with levels
            Rack rackA = createRack(5L, rack1.getStorageToolId());
            createRackLevels(rackA.getRackId(), 5);

            Rack rackB = createRack(4L, rack2.getStorageToolId());
            createRackLevels(rackB.getRackId(), 4);

            Rack rackC = createRack(6L, rack3.getStorageToolId());
            createRackLevels(rackC.getRackId(), 6);

            Rack rackD = createRack(5L, rack4.getStorageToolId());
            createRackLevels(rackD.getRackId(), 5);

            log.info("Seeded {} racks with {} rack levels", rackRepository.count(), rackLevelRepository.count());

            // Seed Fridges with temperature settings
            createFridge(4L, 0L, 8L, fridge1.getStorageToolId()); // Rau củ quả
            createFridge(2L, -2L, 5L, fridge2.getStorageToolId()); // Thịt và hải sản
            createFridge(3L, 1L, 6L, fridge3.getStorageToolId()); // Sữa và trứng
            createFridge(4L, 0L, 8L, fridge4.getStorageToolId()); // Đồ uống tươi

            log.info("Seeded {} fridges", fridgeRepository.count());

            // Seed SubSubcategories (normally from Kafka, but seed for testing)
            SubSubcategory tropicalFruits = createSubSubcategory(1L, "Trái Cây Nhiệt Đới", "Xoài, dứa, đu đủ", null);
            SubSubcategory citrusFruits = createSubSubcategory(2L, "Trái Cây Có Múi", "Cam, chanh, quýt", null);
            SubSubcategory berries = createSubSubcategory(3L, "Quả Mọng", "Dâu tây, việt quất, mâm xôi", null);
            SubSubcategory leafyGreens = createSubSubcategory(4L, "Rau Lá Xanh", "Xà lách, rau bina, cải xoăn", null);
            SubSubcategory rootVegetables = createSubSubcategory(5L, "Củ Quả", "Cà rốt, khoai tây, củ cải", null);
            SubSubcategory tomatoesCucumbers = createSubSubcategory(6L, "Cà Chua & Dưa Leo", "Cà chua và dưa leo tươi", null);
            SubSubcategory mushrooms = createSubSubcategory(7L, "Nấm", "Nấm tươi các loại", null);
            SubSubcategory chicken = createSubSubcategory(8L, "Thịt Gà", "Gà tươi nguyên con và từng phần", null);
            SubSubcategory beef = createSubSubcategory(9L, "Thịt Bò", "Các loại thịt bò tươi", null);
            SubSubcategory pork = createSubSubcategory(10L, "Thịt Heo", "Các loại thịt heo tươi", null);
            SubSubcategory fish = createSubSubcategory(11L, "Cá", "Phi lê cá và cá nguyên con tươi", null);
            SubSubcategory shellfish = createSubSubcategory(12L, "Hải Sản Có Vỏ", "Tôm, cua, tôm hùm", null);
            SubSubcategory freshMilk = createSubSubcategory(13L, "Sữa Tươi", "Sữa nguyên kem, tách béo và ít béo", null);
            SubSubcategory yogurt = createSubSubcategory(14L, "Sữa Chua", "Sữa chua nguyên chất và có hương vị", null);
            SubSubcategory chickenEggs = createSubSubcategory(15L, "Trứng Gà", "Trứng gà trắng và nâu", null);

            log.info("Seeded {} sub-subcategories", subSubcategoryRepository.count());

            // Seed Product Generals (normally from Kafka, but seed for testing) - Vietnamese Fresh Food
            ProductGeneral pgMango = createProductGeneral(101L, "Xoài Cát Hòa Lộc", "Xoài cát Hòa Lộc ngọt thơm đặc sản Tiền Giang",
                "https://example.com/xoai.jpg", tropicalFruits.getSubSubcategoryId(), Unit.KILOGRAM, 1L);

            ProductGeneral pgOrange = createProductGeneral(102L, "Cam Sành", "Cam sành Hà Giang ngọt thanh mọng nước",
                "https://example.com/cam.jpg", citrusFruits.getSubSubcategoryId(), Unit.KILOGRAM, 1L);

            ProductGeneral pgStrawberry = createProductGeneral(103L, "Dâu Tây Đà Lạt", "Dâu tây Đà Lạt tươi ngọt thơm",
                "https://example.com/dau.jpg", berries.getSubSubcategoryId(), Unit.GRAM, 500L);

            ProductGeneral pgSpinach = createProductGeneral(104L, "Rau Bina Baby", "Rau bina baby non mềm đã rửa sạch",
                "https://example.com/bina.jpg", leafyGreens.getSubSubcategoryId(), Unit.GRAM, 250L);

            ProductGeneral pgCarrot = createProductGeneral(105L, "Cà Rốt Đà Lạt", "Cà rốt Đà Lạt giòn ngọt",
                "https://example.com/carot.jpg", rootVegetables.getSubSubcategoryId(), Unit.KILOGRAM, 1L);

            ProductGeneral pgTomato = createProductGeneral(106L, "Cà Chua Bi", "Cà chua bi ngọt chín cây",
                "https://example.com/cachua.jpg", tomatoesCucumbers.getSubSubcategoryId(), Unit.GRAM, 300L);

            ProductGeneral pgMushroom = createProductGeneral(107L, "Nấm Hương Tươi", "Nấm hương tươi thơm ngon",
                "https://example.com/nam.jpg", mushrooms.getSubSubcategoryId(), Unit.GRAM, 200L);

            ProductGeneral pgChickenBreast = createProductGeneral(108L, "Ức Gà Ta", "Ức gà ta tươi không xương không da",
                "https://example.com/ucga.jpg", chicken.getSubSubcategoryId(), Unit.KILOGRAM, 1L);

            ProductGeneral pgBeefSteak = createProductGeneral(109L, "Thăn Nội Bò Úc", "Thăn nội bò Úc cao cấp có vân mỡ",
                "https://example.com/thanbo.jpg", beef.getSubSubcategoryId(), Unit.GRAM, 500L);

            ProductGeneral pgPorkBelly = createProductGeneral(110L, "Ba Chỉ Heo", "Ba chỉ heo tươi 3 lớp",
                "https://example.com/bachi.jpg", pork.getSubSubcategoryId(), Unit.KILOGRAM, 1L);

            ProductGeneral pgSalmon = createProductGeneral(111L, "Phi Lê Cá Hồi Na Uy", "Phi lê cá hồi Na Uy tươi giàu Omega-3",
                "https://example.com/cahoi.jpg", fish.getSubSubcategoryId(), Unit.GRAM, 500L);

            ProductGeneral pgShrimp = createProductGeneral(112L, "Tôm Sú", "Tôm sú tươi size lớn",
                "https://example.com/tom.jpg", shellfish.getSubSubcategoryId(), Unit.KILOGRAM, 1L);

            ProductGeneral pgFreshMilk = createProductGeneral(113L, "Sữa Tươi Nguyên Kem Vinamilk", "Sữa tươi nguyên kem 100%",
                "https://example.com/sua.jpg", freshMilk.getSubSubcategoryId(), Unit.LITER, 1L);

            ProductGeneral pgYogurt = createProductGeneral(114L, "Sữa Chua Vinamilk", "Sữa chua không đường",
                "https://example.com/suachua.jpg", yogurt.getSubSubcategoryId(), Unit.GRAM, 100L);

            ProductGeneral pgEggs = createProductGeneral(115L, "Trứng Gà Tươi", "Trứng gà tươi sạch size lớn",
                "https://example.com/trung.jpg", chickenEggs.getSubSubcategoryId(), Unit.DOZEN, 10L);

            log.info("Seeded {} product generals", productGeneralRepository.count());

            // Seed Product Batches - Fresh Food Stock with expiry dates
            LocalDateTime now = LocalDateTime.now();

            // Trái cây - hạn sử dụng 5-10 ngày
            createProductBatch(50L, Unit.KILOGRAM, "Lô xoài Hòa Lộc vụ mùa",
                now, now.plusDays(7), 1001L, tropicalFruits.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(80L, Unit.KILOGRAM, "Cam sành Hà Giang hộp 15kg",
                now, now.plusDays(10), 1002L, citrusFruits.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(10000L, Unit.GRAM, "Dâu tây Đà Lạt hộp 500g x20",
                now, now.plusDays(5), 1003L, berries.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            // Rau củ - hạn sử dụng 3-7 ngày
            createProductBatch(5000L, Unit.GRAM, "Rau bina baby túi 250g x20",
                now, now.plusDays(4), 1004L, leafyGreens.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(100L, Unit.KILOGRAM, "Cà rốt Đà Lạt bao 10kg",
                now, now.plusDays(7), 1005L, rootVegetables.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(9000L, Unit.GRAM, "Cà chua bi hộp 300g x30",
                now, now.plusDays(6), 1006L, tomatoesCucumbers.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(4000L, Unit.GRAM, "Nấm hương tươi hộp 200g x20",
                now, now.plusDays(5), 1007L, mushrooms.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            // Thịt - hạn sử dụng 2-3 ngày (tươi), 30 ngày (đông lạnh)
            createProductBatch(50L, Unit.KILOGRAM, "Ức gà ta tươi nguyên khối",
                now, now.plusDays(3), 2001L, chicken.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(20000L, Unit.GRAM, "Thăn nội bò Úc khối 500g x40",
                now.minusDays(1), now.plusDays(29), 2002L, beef.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(80L, Unit.KILOGRAM, "Ba chỉ heo tươi nguyên khối",
                now, now.plusDays(2), 2003L, pork.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            // Hải sản - hạn sử dụng 1-2 ngày (tươi), 15-30 ngày (đông lạnh)
            createProductBatch(15000L, Unit.GRAM, "Phi lê cá hồi Na Uy 500g x30",
                now, now.plusDays(15), 3001L, fish.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(30L, Unit.KILOGRAM, "Tôm sú tươi size 20-25 con/kg",
                now, now.plusDays(2), 3002L, shellfish.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            // Sữa và trứng - hạn sử dụng 5-15 ngày
            createProductBatch(100L, Unit.LITER, "Sữa tươi Vinamilk hộp 1L x100",
                now, now.plusDays(7), 4001L, freshMilk.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(10000L, Unit.GRAM, "Sữa chua Vinamilk hộp 100g x100",
                now, now.plusDays(10), 4002L, yogurt.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            createProductBatch(50L, Unit.DOZEN, "Trứng gà tươi vỉ 10 quả x50",
                now, now.plusDays(14), 4003L, chickenEggs.getSubSubcategoryId(), ProductBatchProcessStatus.PENDING);

            log.info("Seeded {} product batches", productBatchRepository.count());

            log.info("Database seeding completed successfully!");
            log.info("Summary:");
            log.info("  - {} warehouses with storage capacity", warehouseRepository.count());
            log.info("  - {} storage tools ({} racks + {} fridges)",
                storageToolRepository.count(), rackRepository.count(), fridgeRepository.count());
            log.info("  - {} product categories ready for inventory", subSubcategoryRepository.count());
            log.info("  - {} product types available", productGeneralRepository.count());
            log.info("  - {} fresh batches ready to process", productBatchRepository.count());
        };
    }

    private Warehouse createWarehouse(String address, Long usagePercentage, Long numOfFridge, Long numOfRack) {
        Warehouse warehouse = new Warehouse(address, usagePercentage, numOfFridge, numOfRack);
        return warehouseRepository.save(warehouse);
    }

    private StorageTool createStorageTool(LocalDate lastMaintainanceDate, StorageToolStatus status,
                                         Long usagePercentage, Long warehouseId, StorageType toolType) {
        StorageTool storageTool = new StorageTool(lastMaintainanceDate, status, usagePercentage, warehouseId, toolType);
        return storageToolRepository.save(storageTool);
    }

    private Rack createRack(Long numOfLevel, Long storageToolId) {
        Rack rack = new Rack(numOfLevel, storageToolId);
        return rackRepository.save(rack);
    }

    private void createRackLevels(Long rackId, int numOfLevels) {
        for (int i = 0; i < numOfLevels; i++) {
            RackLevel level = new RackLevel(0L, rackId);
            rackLevelRepository.save(level);
        }
    }

    private Fridge createFridge(Long curTemp, Long minTemp, Long maxTemp, Long storageToolId) {
        Fridge fridge = new Fridge(curTemp, minTemp, maxTemp, storageToolId);
        return fridgeRepository.save(fridge);
    }

    private SubSubcategory createSubSubcategory(Long id, String name, String description, String iconUrl) {
        SubSubcategory subSubcategory = new SubSubcategory();
        subSubcategory.setSubSubcategoryId(id);
        subSubcategory.setName(name);
        subSubcategory.setDescription(description);
        subSubcategory.setIconUrl(iconUrl);
        return subSubcategoryRepository.save(subSubcategory);
    }

    private ProductGeneral createProductGeneral(Long prodGenId, String name, String description,
                                               String imgUrl, Long subSubcategoryId, Unit unit, Long unitQuantity) {
        ProductGeneral productGeneral = new ProductGeneral();
        productGeneral.setProdGenId(prodGenId);
        productGeneral.setName(name);
        productGeneral.setDescription(description);
        productGeneral.setImgUrl(imgUrl);
        productGeneral.setSubSubcategoryId(subSubcategoryId);
        productGeneral.setUnit(unit);
        productGeneral.setUnitQuantity(unitQuantity);
        return productGeneralRepository.save(productGeneral);
    }

    private ProductBatch createProductBatch(Long quantity, Unit unit, String note,
                                           LocalDateTime receivedAt, LocalDateTime expiredAt,
                                           Long providerId, Long subSubcategoryId,
                                           ProductBatchProcessStatus processStatus) {
        ProductBatch batch = new ProductBatch(quantity, unit, note, receivedAt, expiredAt, providerId, subSubcategoryId);
        batch.setProcessStatus(processStatus);
        return productBatchRepository.save(batch);
    }
}

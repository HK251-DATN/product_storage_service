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
            createSubSubcategory(1L, "Thịt Gà", "Thịt gà tươi nguyên con và các phần", null, 2L);
            createSubSubcategory(2L, "Thịt Vịt", "Thịt vịt tươi nguyên con và các phần", null, 2L);
            createSubSubcategory(3L, "Thịt Ngan", "Thịt ngan tươi", null, 2L);
            createSubSubcategory(4L, "Thịt Chim Cút", "Chim cút tươi nguyên con", null, 2L);
            createSubSubcategory(5L, "Lòng Gia Cầm", "Gan, mề, tim gia cầm tươi", null, 2L);
            createSubSubcategory(6L, "Thịt Bò", "Thịt bò tươi các loại", null, 3L);
            createSubSubcategory(7L, "Thịt Heo", "Thịt heo tươi các loại", null, 3L);
            createSubSubcategory(8L, "Thịt Dê", "Thịt dê tươi", null, 3L);
            createSubSubcategory(9L, "Thịt Cừu", "Thịt cừu tươi nhập khẩu", null, 3L);
            createSubSubcategory(10L, "Xúc Xích Tươi", "Xúc xích tươi chưa qua chế biến", null, 3L);
            createSubSubcategory(11L, "Tôm Tươi", "Tôm sú, tôm thẻ tươi sống", null, 4L);
            createSubSubcategory(12L, "Cá Tươi", "Các loại cá tươi nguyên con và phi lê", null, 4L);
            createSubSubcategory(13L, "Mực Tươi", "Mực ống, mực nang tươi", null, 4L);
            createSubSubcategory(14L, "Cua Ghẹ", "Cua biển, ghẹ tươi sống", null, 4L);
            createSubSubcategory(15L, "Nghêu Sò", "Nghêu, sò, hàu tươi", null, 4L);
            createSubSubcategory(16L, "Rau Muống", "Rau muống tươi", null, 6L);
            createSubSubcategory(17L, "Cải Xanh", "Cải xanh, cải ngọt tươi", null, 6L);
            createSubSubcategory(18L, "Xà Lách", "Xà lách các loại tươi", null, 6L);
            createSubSubcategory(19L, "Rau Dền", "Rau dền đỏ và rau dền xanh", null, 6L);
            createSubSubcategory(20L, "Cải Thìa", "Cải thìa, cải bẹ trắng tươi", null, 6L);
            createSubSubcategory(21L, "Cà Rốt", "Cà rốt tươi", null, 7L);
            createSubSubcategory(22L, "Khoai Tây", "Khoai tây tươi", null, 7L);
            createSubSubcategory(23L, "Củ Cải", "Củ cải trắng, củ cải đỏ tươi", null, 7L);
            createSubSubcategory(24L, "Bắp", "Bắp ngô tươi các loại", null, 7L);
            createSubSubcategory(25L, "Su Su", "Su su tươi", null, 7L);
            createSubSubcategory(26L, "Xoài", "Xoài tươi các loại", null, 8L);
            createSubSubcategory(27L, "Chuối", "Chuối tươi các loại", null, 8L);
            createSubSubcategory(28L, "Dưa Hấu", "Dưa hấu tươi", null, 8L);
            createSubSubcategory(29L, "Ổi", "Ổi tươi các loại", null, 8L);
            createSubSubcategory(30L, "Thanh Long", "Thanh long ruột đỏ và ruột trắng", null, 8L);
            createSubSubcategory(31L, "Hành Lá", "Hành lá tươi", null, 9L);
            createSubSubcategory(32L, "Tỏi", "Tỏi tươi và tỏi khô", null, 9L);
            createSubSubcategory(33L, "Gừng", "Gừng tươi", null, 9L);
            createSubSubcategory(34L, "Ớt", "Ớt sừng, ớt hiểm tươi", null, 9L);
            createSubSubcategory(35L, "Sả", "Sả tươi nguyên cây", null, 9L);
            createSubSubcategory(36L, "Sữa Tươi Không Đường", "Sữa tươi tiệt trùng không đường", null, 11L);
            createSubSubcategory(37L, "Sữa Tươi Có Đường", "Sữa tươi tiệt trùng có đường", null, 11L);
            createSubSubcategory(38L, "Sữa Hữu Cơ", "Sữa tươi hữu cơ nguyên chất", null, 11L);
            createSubSubcategory(39L, "Sữa Ít Béo", "Sữa tươi ít béo tách một phần kem", null, 11L);
            createSubSubcategory(40L, "Sữa Tách Béo", "Sữa tươi tách hoàn toàn chất béo", null, 11L);
            createSubSubcategory(41L, "Bơ Tươi", "Bơ động vật tươi các loại", null, 12L);
            createSubSubcategory(42L, "Phô Mai", "Phô mai tươi và phô mai chế biến", null, 12L);
            createSubSubcategory(43L, "Kem Tươi", "Kem tươi whipping cream", null, 12L);
            createSubSubcategory(44L, "Sữa Chua", "Sữa chua ăn các loại", null, 12L);
            createSubSubcategory(45L, "Sữa Đặc", "Sữa đặc có đường và không đường", null, 12L);
            createSubSubcategory(46L, "Trứng Gà Công Nghiệp", "Trứng gà tươi công nghiệp", null, 13L);
            createSubSubcategory(47L, "Trứng Gà Ta", "Trứng gà ta thả vườn", null, 13L);
            createSubSubcategory(48L, "Trứng Vịt", "Trứng vịt tươi", null, 13L);
            createSubSubcategory(49L, "Trứng Cút", "Trứng cút tươi", null, 13L);
            createSubSubcategory(50L, "Trứng Vịt Lộn", "Trứng vịt lộn ấp sẵn", null, 13L);
            createSubSubcategory(51L, "Sữa Đậu Nành", "Sữa đậu nành tươi nguyên chất", null, 14L);
            createSubSubcategory(52L, "Sữa Hạt", "Sữa hạnh nhân, sữa óc chó, sữa hạt điều", null, 14L);
            createSubSubcategory(53L, "Yaourt Uống", "Sữa chua uống các loại", null, 14L);
            createSubSubcategory(54L, "Kefir", "Kefir lên men tự nhiên", null, 14L);
            createSubSubcategory(55L, "Sữa Chua Uống Nha Đam", "Sữa chua uống kết hợp nha đam", null, 14L);
            
            log.info("Seeded {} sub-subcategories", subSubcategoryRepository.count());
//            SubSubcategory tropicalFruits = createSubSubcategory(1L, "Trái Cây Nhiệt Đới", "Xoài, dứa, đu đủ", null);
//            SubSubcategory citrusFruits = createSubSubcategory(2L, "Trái Cây Có Múi", "Cam, chanh, quýt", null);
//            SubSubcategory berries = createSubSubcategory(3L, "Quả Mọng", "Dâu tây, việt quất, mâm xôi", null);
//            SubSubcategory leafyGreens = createSubSubcategory(4L, "Rau Lá Xanh", "Xà lách, rau bina, cải xoăn", null);
//            SubSubcategory rootVegetables = createSubSubcategory(5L, "Củ Quả", "Cà rốt, khoai tây, củ cải", null);
//            SubSubcategory tomatoesCucumbers = createSubSubcategory(6L, "Cà Chua & Dưa Leo", "Cà chua và dưa leo tươi", null);
//            SubSubcategory mushrooms = createSubSubcategory(7L, "Nấm", "Nấm tươi các loại", null);
//            SubSubcategory chicken = createSubSubcategory(8L, "Thịt Gà", "Gà tươi nguyên con và từng phần", null);
//            SubSubcategory beef = createSubSubcategory(9L, "Thịt Bò", "Các loại thịt bò tươi", null);
//            SubSubcategory pork = createSubSubcategory(10L, "Thịt Heo", "Các loại thịt heo tươi", null);
//            SubSubcategory fish = createSubSubcategory(11L, "Cá", "Phi lê cá và cá nguyên con tươi", null);
//            SubSubcategory shellfish = createSubSubcategory(12L, "Hải Sản Có Vỏ", "Tôm, cua, tôm hùm", null);
//            SubSubcategory freshMilk = createSubSubcategory(13L, "Sữa Tươi", "Sữa nguyên kem, tách béo và ít béo", null);
//            SubSubcategory yogurt = createSubSubcategory(14L, "Sữa Chua", "Sữa chua nguyên chất và có hương vị", null);
//            SubSubcategory chickenEggs = createSubSubcategory(15L, "Trứng Gà", "Trứng gà trắng và nâu", null);
            
            
            // Seed Sample Product Generals - Fresh Food (Vietnamese)
            
            // Thịt Gà (subSubcategoryId: 1)
            createProductGeneral(1L, "Gà Ta Nguyên Con", "Gà ta thả vườn tươi ngon, thịt chắc thơm ngon", new String[]{"gà ta", "gà nguyên con", "thịt gà"}, Unit.KILOGRAM, 1L, 1L);
            createProductGeneral(2L, "Ức Gà Phi Lê", "Ức gà phi lê không xương, thịt trắng mềm", new String[]{"ức gà", "phi lê", "thịt gà"}, Unit.GRAM, 500L, 1L);
            createProductGeneral(3L, "Đùi Gà Tươi", "Đùi gà tươi có xương, thịt ngọt đậm đà", new String[]{"đùi gà", "thịt gà", "gà tươi"}, Unit.GRAM, 500L, 1L);
            
            // Thịt Vịt (subSubcategoryId: 2)
            createProductGeneral(4L, "Vịt Trời Nguyên Con", "Vịt trời tươi nguyên con, thịt chắc thơm", new String[]{"vịt trời", "vịt nguyên con", "thịt vịt"}, Unit.KILOGRAM, 1L, 2L);
            createProductGeneral(5L, "Ức Vịt Phi Lê", "Ức vịt phi lê không da, ít mỡ", new String[]{"ức vịt", "phi lê", "thịt vịt"}, Unit.GRAM, 500L, 2L);
            createProductGeneral(6L, "Đùi Vịt Bó Xôi", "Đùi vịt bó xôi tươi ngon, thịt đậm đà", new String[]{"đùi vịt", "vịt bó xôi", "thịt vịt"}, Unit.GRAM, 500L, 2L);
            
            // Thịt Ngan (subSubcategoryId: 3)
            createProductGeneral(7L, "Ngan Nguyên Con", "Ngan tươi nguyên con, thịt thơm béo", new String[]{"ngan", "ngan nguyên con", "thịt ngan"}, Unit.KILOGRAM, 1L, 3L);
            createProductGeneral(8L, "Ức Ngan Phi Lê", "Ức ngan phi lê cao cấp, thịt đỏ thơm", new String[]{"ức ngan", "phi lê", "thịt ngan"}, Unit.GRAM, 500L, 3L);
            createProductGeneral(9L, "Đùi Ngan Tươi", "Đùi ngan tươi ngon, thịt chắc", new String[]{"đùi ngan", "thịt ngan", "ngan tươi"}, Unit.GRAM, 500L, 3L);
            
            // Thịt Chim Cút (subSubcategoryId: 4)
            createProductGeneral(10L, "Chim Cút Nguyên Con", "Chim cút tươi nguyên con, thịt thơm ngọt", new String[]{"chim cút", "cút nguyên con", "thịt cút"}, Unit.GRAM, 200L, 4L);
            createProductGeneral(11L, "Chim Cút Rút Xương", "Chim cút rút xương sẵn, tiện chế biến", new String[]{"chim cút", "rút xương", "thịt cút"}, Unit.GRAM, 200L, 4L);
            createProductGeneral(12L, "Chim Cút Loại 1", "Chim cút loại 1 to đều, tươi ngon", new String[]{"chim cút", "cút loại 1", "thịt cút"}, Unit.GRAM, 300L, 4L);
            
            // Lòng Gia Cầm (subSubcategoryId: 5)
            createProductGeneral(13L, "Gan Gà Tươi", "Gan gà tươi ngon, giàu dinh dưỡng", new String[]{"gan gà", "lòng gà", "nội t장"}, Unit.GRAM, 300L, 5L);
            createProductGeneral(14L, "Mề Gà Tươi", "Mề gà tươi sạch, giòn ngọt", new String[]{"mề gà", "lòng gà", "dạ dày gà"}, Unit.GRAM, 300L, 5L);
            createProductGeneral(15L, "Tim Gà Tươi", "Tim gà tươi ngon, dai giòn", new String[]{"tim gà", "lòng gà", "nội tạng"}, Unit.GRAM, 200L, 5L);
            
            // Thịt Bò (subSubcategoryId: 6)
            createProductGeneral(16L, "Thịt Bò Úc Phi Lê", "Thịt bò Úc nhập khẩu phi lê mềm", new String[]{"thịt bò", "bò Úc", "phi lê"}, Unit.GRAM, 500L, 6L);
            createProductGeneral(17L, "Thịt Bò Nạm", "Thịt bò nạm tươi, thích hợp nấu phở", new String[]{"thịt bò", "bò nạm", "bò tươi"}, Unit.GRAM, 500L, 6L);
            createProductGeneral(18L, "Thịt Bò Vai", "Thịt bò vai tươi ngon, thơm mềm", new String[]{"thịt bò", "bò vai", "bò tươi"}, Unit.GRAM, 500L, 6L);
            
            // Thịt Heo (subSubcategoryId: 7)
            createProductGeneral(19L, "Thịt Heo Ba Chỉ", "Thịt heo ba chỉ tươi, vừa nạc vừa mỡ", new String[]{"thịt heo", "ba chỉ", "thịt lợn"}, Unit.GRAM, 500L, 7L);
            createProductGeneral(20L, "Thịt Heo Nạc Vai", "Thịt heo nạc vai tươi, ít mỡ", new String[]{"thịt heo", "nạc vai", "thịt lợn"}, Unit.GRAM, 500L, 7L);
            createProductGeneral(21L, "Thịt Heo Nạc Dăm", "Thịt heo nạc dăm tươi ngon", new String[]{"thịt heo", "nạc dăm", "thịt lợn"}, Unit.GRAM, 500L, 7L);
            
            // Thịt Dê (subSubcategoryId: 8)
            createProductGeneral(22L, "Thịt Dê Nạc", "Thịt dê nạc tươi, thơm ngon bổ dưỡng", new String[]{"thịt dê", "dê nạc", "dê tươi"}, Unit.GRAM, 500L, 8L);
            createProductGeneral(23L, "Thịt Dê Có Xương", "Thịt dê có xương nấu cháo, hầm", new String[]{"thịt dê", "dê xương", "dê tươi"}, Unit.GRAM, 500L, 8L);
            createProductGeneral(24L, "Sườn Dê Tươi", "Sườn dê tươi ngon, nướng hoặc hầm", new String[]{"sườn dê", "thịt dê", "dê tươi"}, Unit.GRAM, 500L, 8L);
            
            // Thịt Cừu (subSubcategoryId: 9)
            createProductGeneral(25L, "Thịt Cừu Úc Phi Lê", "Thịt cừu Úc nhập khẩu phi lê cao cấp", new String[]{"thịt cừu", "cừu Úc", "phi lê"}, Unit.GRAM, 500L, 9L);
            createProductGeneral(26L, "Thịt Cừu New Zealand", "Thịt cừu New Zealand tươi ngon", new String[]{"thịt cừu", "cừu NZ", "cừu nhập khẩu"}, Unit.GRAM, 500L, 9L);
            createProductGeneral(27L, "Sườn Cừu Cao Cấp", "Sườn cừu cao cấp nướng BBQ", new String[]{"sườn cừu", "thịt cừu", "cừu nướng"}, Unit.GRAM, 500L, 9L);
            
            // Xúc Xích Tươi (subSubcategoryId: 10)
            createProductGeneral(28L, "Xúc Xích Heo Tươi", "Xúc xích heo tươi chưa nướng", new String[]{"xúc xích", "xúc xích heo", "lạp xưởng"}, Unit.GRAM, 500L, 10L);
            createProductGeneral(29L, "Xúc Xích Bò Tươi", "Xúc xích bò tươi nguyên chất", new String[]{"xúc xích", "xúc xích bò", "lạp xưởng"}, Unit.GRAM, 500L, 10L);
            createProductGeneral(30L, "Xúc Xích Gà Tươi", "Xúc xích gà tươi ít béo, healthy", new String[]{"xúc xích", "xúc xích gà", "lạp xưởng"}, Unit.GRAM, 500L, 10L);
            
            // Tôm Tươi (subSubcategoryId: 11)
            createProductGeneral(31L, "Tôm Sú Tươi Sống", "Tôm sú tươi sống size lớn", new String[]{"tôm sú", "tôm tươi", "hải sản"}, Unit.KILOGRAM, 1L, 11L);
            createProductGeneral(32L, "Tôm Thẻ Tươi", "Tôm thẻ tươi ngọt thịt chắc", new String[]{"tôm thẻ", "tôm tươi", "hải sản"}, Unit.GRAM, 500L, 11L);
            createProductGeneral(33L, "Tôm Càng Xanh", "Tôm càng xanh tươi sống to", new String[]{"tôm càng", "tôm tươi", "hải sản"}, Unit.KILOGRAM, 1L, 11L);
            
            // Cá Tươi (subSubcategoryId: 12)
            createProductGeneral(34L, "Cá Hồi Phi Lê", "Cá hồi phi lê tươi Nauy", new String[]{"cá hồi", "phi lê", "cá tươi"}, Unit.GRAM, 500L, 12L);
            createProductGeneral(35L, "Cá Basa Phi Lê", "Cá basa phi lê không xương", new String[]{"cá basa", "phi lê", "cá tươi"}, Unit.GRAM, 500L, 12L);
            createProductGeneral(36L, "Cá Diêu Hồng Tươi", "Cá diêu hồng tươi nguyên con", new String[]{"cá diêu hồng", "cá nguyên con", "cá tươi"}, Unit.KILOGRAM, 1L, 12L);
            
            // Mực Tươi (subSubcategoryId: 13)
            createProductGeneral(37L, "Mực Ống Tươi", "Mực ống tươi size lớn", new String[]{"mực ống", "mực tươi", "hải sản"}, Unit.KILOGRAM, 1L, 13L);
            createProductGeneral(38L, "Mực Nang Tươi", "Mực nang tươi ngọt thịt dai", new String[]{"mực nang", "mực tươi", "hải sản"}, Unit.GRAM, 500L, 13L);
            createProductGeneral(39L, "Mực Lá Tươi", "Mực lá tươi sống nhỏ", new String[]{"mực lá", "mực tươi", "hải sản"}, Unit.GRAM, 500L, 13L);
            
            // Cua Ghẹ (subSubcategoryId: 14)
            createProductGeneral(40L, "Cua Biển Tươi Sống", "Cua biển tươi sống size to", new String[]{"cua biển", "cua tươi", "hải sản"}, Unit.KILOGRAM, 1L, 14L);
            createProductGeneral(41L, "Ghẹ Xanh Tươi", "Ghẹ xanh tươi sống thịt ngọt", new String[]{"ghẹ", "cua tươi", "hải sản"}, Unit.KILOGRAM, 1L, 14L);
            createProductGeneral(42L, "Cua Gạch Tươi", "Cua gạch tươi đầy gạch béo ngậy", new String[]{"cua gạch", "cua tươi", "hải sản"}, Unit.KILOGRAM, 1L, 14L);
            
            // Nghêu Sò (subSubcategoryId: 15)
            createProductGeneral(43L, "Nghêu Tươi Sống", "Nghêu tươi sống vỏ to thịt ngọt", new String[]{"nghêu", "sò tươi", "hải sản"}, Unit.KILOGRAM, 1L, 15L);
            createProductGeneral(44L, "Sò Huyết Tươi", "Sò huyết tươi sống ngon ngọt", new String[]{"sò huyết", "sò tươi", "hải sản"}, Unit.KILOGRAM, 1L, 15L);
            createProductGeneral(45L, "Hàu Sữa Tươi", "Hàu sữa tươi sống béo ngậy", new String[]{"hàu", "sò tươi", "hải sản"}, Unit.KILOGRAM, 1L, 15L);
            
            // Rau Muống (subSubcategoryId: 16)
            createProductGeneral(46L, "Rau Muống Xanh", "Rau muống xanh tươi non mơn mởn", new String[]{"rau muống", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 16L);
            createProductGeneral(47L, "Rau Muống Dại", "Rau muống dại tươi thơm ngon", new String[]{"rau muống", "rau dại", "rau xanh"}, Unit.GRAM, 500L, 16L);
            createProductGeneral(48L, "Rau Muống Cọng To", "Rau muống cọng to tươi giòn", new String[]{"rau muống", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 16L);
            
            // Cải Xanh (subSubcategoryId: 17)
            createProductGeneral(49L, "Cải Xanh Tươi", "Cải xanh tươi ngọt mát", new String[]{"cải xanh", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 17L);
            createProductGeneral(50L, "Cải Ngọt Baby", "Cải ngọt baby non mềm", new String[]{"cải ngọt", "rau xanh", "rau ăn lá"}, Unit.GRAM, 300L, 17L);
            createProductGeneral(51L, "Cải Ngồng Tươi", "Cải ngồng tươi giòn ngọt", new String[]{"cải ngồng", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 17L);
            
            // Xà Lách (subSubcategoryId: 18)
            createProductGeneral(52L, "Xà Lách Xoong", "Xà lách xoong tươi giòn", new String[]{"xà lách", "rau xanh", "rau salad"}, Unit.GRAM, 300L, 18L);
            createProductGeneral(53L, "Xà Lách Lô Lô", "Xà lách lô lô đỏ tím tươi", new String[]{"xà lách", "rau xanh", "rau salad"}, Unit.GRAM, 300L, 18L);
            createProductGeneral(54L, "Xà Lách Tím", "Xà lách tím tươi giàu anthocyanin", new String[]{"xà lách", "rau xanh", "rau salad"}, Unit.GRAM, 300L, 18L);
            
            // Rau Dền (subSubcategoryId: 19)
            createProductGeneral(55L, "Rau Dền Đỏ", "Rau dền đỏ tươi giàu sắt", new String[]{"rau dền", "rau đỏ", "rau xanh"}, Unit.GRAM, 500L, 19L);
            createProductGeneral(56L, "Rau Dền Xanh", "Rau dền xanh tươi mát", new String[]{"rau dền", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 19L);
            createProductGeneral(57L, "Rau Dền Cơm", "Rau dền cơm non mềm thơm", new String[]{"rau dền", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 19L);
            
            // Cải Thìa (subSubcategoryId: 20)
            createProductGeneral(58L, "Cải Thìa Tươi", "Cải thìa tươi ngọt mát", new String[]{"cải thìa", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 20L);
            createProductGeneral(59L, "Cải Bẹ Trắng", "Cải bẹ trắng tươi giòn ngọt", new String[]{"cải bẹ", "rau xanh", "rau ăn lá"}, Unit.GRAM, 500L, 20L);
            createProductGeneral(60L, "Cải Thìa Baby", "Cải thìa baby non mềm", new String[]{"cải thìa", "rau xanh", "rau ăn lá"}, Unit.GRAM, 300L, 20L);
            
            // Cà Rốt (subSubcategoryId: 21)
            createProductGeneral(61L, "Cà Rốt Đà Lạt", "Cà rốt Đà Lạt tươi giòn ngọt", new String[]{"cà rốt", "củ quả", "rau củ"}, Unit.GRAM, 500L, 21L);
            createProductGeneral(62L, "Cà Rốt Nhật", "Cà rốt Nhật to đều màu đẹp", new String[]{"cà rốt", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 21L);
            createProductGeneral(63L, "Cà Rốt Baby", "Cà rốt baby nhỏ xinh ăn salad", new String[]{"cà rốt", "củ quả", "rau củ"}, Unit.GRAM, 300L, 21L);
            
            // Khoai Tây (subSubcategoryId: 22)
            createProductGeneral(64L, "Khoai Tây Đà Lạt", "Khoai tây Đà Lạt tươi ngon", new String[]{"khoai tây", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 22L);
            createProductGeneral(65L, "Khoai Tây Ai Cập", "Khoai tây Ai Cập nhập khẩu", new String[]{"khoai tây", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 22L);
            createProductGeneral(66L, "Khoai Tây Tím", "Khoai tây tím giàu chất chống oxy hóa", new String[]{"khoai tây", "củ quả", "rau củ"}, Unit.GRAM, 500L, 22L);
            
            // Củ Cải (subSubcategoryId: 23)
            createProductGeneral(67L, "Củ Cải Trắng", "Củ cải trắng tươi giòn ngọt", new String[]{"củ cải", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 23L);
            createProductGeneral(68L, "Củ Cải Đỏ", "Củ cải đỏ tươi giàu vitamin", new String[]{"củ cải", "củ quả", "rau củ"}, Unit.GRAM, 500L, 23L);
            createProductGeneral(69L, "Củ Cải Muối", "Củ cải trắng dùng làm dưa muối", new String[]{"củ cải", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 23L);
            
            // Bắp (subSubcategoryId: 24)
            createProductGeneral(70L, "Bắp Ngọt Tươi", "Bắp ngọt tươi hạt vàng căng mọng", new String[]{"bắp ngô", "ngô", "rau củ"}, Unit.KILOGRAM, 1L, 24L);
            createProductGeneral(71L, "Bắp Nếp Tươi", "Bắp nếp tươi dẻo thơm", new String[]{"bắp nếp", "ngô", "rau củ"}, Unit.KILOGRAM, 1L, 24L);
            createProductGeneral(72L, "Bắp Mỹ Tươi", "Bắp Mỹ tươi hạt to ngọt", new String[]{"bắp ngô", "ngô Mỹ", "rau củ"}, Unit.KILOGRAM, 1L, 24L);
            
            // Su Su (subSubcategoryId: 25)
            createProductGeneral(73L, "Su Su Xanh", "Su su xanh tươi giòn ngọt", new String[]{"su su", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 25L);
            createProductGeneral(74L, "Su Su Trắng", "Su su trắng tươi mềm ngọt", new String[]{"su su", "củ quả", "rau củ"}, Unit.KILOGRAM, 1L, 25L);
            createProductGeneral(75L, "Su Su Non", "Su su non tươi giòn ăn salad", new String[]{"su su", "củ quả", "rau củ"}, Unit.GRAM, 500L, 25L);
            
            // Xoài (subSubcategoryId: 26)
            createProductGeneral(76L, "Xoài Cát Hòa Lộc", "Xoài cát Hòa Lộc ngọt thơm", new String[]{"xoài", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 26L);
            createProductGeneral(77L, "Xoài Úc", "Xoài Úc nhập khẩu to ngọt", new String[]{"xoài", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 26L);
            createProductGeneral(78L, "Xoài Tượng", "Xoài tượng xanh giòn chua ngọt", new String[]{"xoài", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 26L);
            
            // Chuối (subSubcategoryId: 27)
            createProductGeneral(79L, "Chuối Già", "Chuối già ngọt thơm bổ dưỡng", new String[]{"chuối", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 27L);
            createProductGeneral(80L, "Chuối Tiêu Hương", "Chuối tiêu hương thơm ngọt", new String[]{"chuối", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 27L);
            createProductGeneral(81L, "Chuối Sứ", "Chuối sứ nhỏ ngọt đậm", new String[]{"chuối", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 27L);
            
            // Dưa Hấu (subSubcategoryId: 28)
            createProductGeneral(82L, "Dưa Hấu Không Hạt", "Dưa hấu không hạt ngọt mát", new String[]{"dưa hấu", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 28L);
            createProductGeneral(83L, "Dưa Hấu Ruột Đỏ", "Dưa hấu ruột đỏ ngọt tươi", new String[]{"dưa hấu", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 28L);
            createProductGeneral(84L, "Dưa Hấu Vàng", "Dưa hấu vàng giòn ngọt thanh", new String[]{"dưa hấu", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 28L);
            
            // Ổi (subSubcategoryId: 29)
            createProductGeneral(85L, "Ổi Nữ Hoàng", "Ổi nữ hoàng giòn ngọt thơm", new String[]{"ổi", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 29L);
            createProductGeneral(86L, "Ổi Ruột Đỏ", "Ổi ruột đỏ ngọt giàu lycopene", new String[]{"ổi", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 29L);
            createProductGeneral(87L, "Ổi Xanh", "Ổi xanh giòn chua nhẹ", new String[]{"ổi", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 29L);
            
            // Thanh Long (subSubcategoryId: 30)
            createProductGeneral(88L, "Thanh Long Ruột Đỏ", "Thanh long ruột đỏ ngọt thơm", new String[]{"thanh long", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 30L);
            createProductGeneral(89L, "Thanh Long Ruột Trắng", "Thanh long ruột trắng ngọt mát", new String[]{"thanh long", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 30L);
            createProductGeneral(90L, "Thanh Long Vàng", "Thanh long vàng Ecuador cao cấp", new String[]{"thanh long", "trái cây", "hoa quả"}, Unit.KILOGRAM, 1L, 30L);
            
            // Hành Lá (subSubcategoryId: 31)
            createProductGeneral(91L, "Hành Lá Tươi", "Hành lá tươi thơm dùng nêm nếm", new String[]{"hành lá", "gia vị", "rau thơm"}, Unit.GRAM, 200L, 31L);
            createProductGeneral(92L, "Hành Tây Tươi", "Hành tây tươi cay thơm", new String[]{"hành tây", "gia vị", "rau thơm"}, Unit.KILOGRAM, 1L, 31L);
            createProductGeneral(93L, "Hành Tím Tươi", "Hành tím tươi cay nồng", new String[]{"hành tím", "gia vị", "rau thơm"}, Unit.GRAM, 500L, 31L);
            
            // Tỏi (subSubcategoryId: 32)
            createProductGeneral(94L, "Tỏi Lý Sơn", "Tỏi Lý Sơn đặc sản thơm cay", new String[]{"tỏi", "gia vị", "rau thơm"}, Unit.GRAM, 500L, 32L);
            createProductGeneral(95L, "Tỏi Tươi Cà Mau", "Tỏi tươi Cà Mau múi to", new String[]{"tỏi", "gia vị", "rau thơm"}, Unit.GRAM, 500L, 32L);
            createProductGeneral(96L, "Tỏi Tây", "Tỏi tây nhập khẩu múi lớn", new String[]{"tỏi", "gia vị", "rau thơm"}, Unit.GRAM, 500L, 32L);
            
            // Gừng (subSubcategoryId: 33)
            createProductGeneral(97L, "Gừng Già", "Gừng già cay nồng dùng nấu ăn", new String[]{"gừng", "gia vị", "rau củ"}, Unit.GRAM, 500L, 33L);
            createProductGeneral(98L, "Gừng Non", "Gừng non ít cay giòn ngọt", new String[]{"gừng", "gia vị", "rau củ"}, Unit.GRAM, 300L, 33L);
            createProductGeneral(99L, "Gừng Khô", "Gừng khô cay thơm lâu", new String[]{"gừng", "gia vị", "rau củ"}, Unit.GRAM, 200L, 33L);
            
            // Ớt (subSubcategoryId: 34)
            createProductGeneral(100L, "Ớt Sừng Xanh", "Ớt sừng xanh tươi cay nhẹ", new String[]{"ớt", "gia vị", "rau thơm"}, Unit.GRAM, 200L, 34L);
            createProductGeneral(101L, "Ớt Hiểm Đỏ", "Ớt hiểm đỏ tươi cay nồng", new String[]{"ớt", "gia vị", "rau thơm"}, Unit.GRAM, 200L, 34L);
            createProductGeneral(102L, "Ớt Chuông", "Ớt chuông tươi ngọt màu sắc", new String[]{"ớt", "gia vị", "rau thơm"}, Unit.GRAM, 500L, 34L);
            
            // Sả (subSubcategoryId: 35)
            createProductGeneral(103L, "Sả Tươi Nguyên Cây", "Sả tươi nguyên cây thơm nồng", new String[]{"sả", "gia vị", "rau thơm"}, Unit.GRAM, 300L, 35L);
            createProductGeneral(104L, "Sả Tía Tươi", "Sả tía tươi thơm đặc biệt", new String[]{"sả", "gia vị", "rau thơm"}, Unit.GRAM, 300L, 35L);
            createProductGeneral(105L, "Sả Tôm Tươi", "Sả tôm tươi thơm nhẹ", new String[]{"sả", "gia vị", "rau thơm"}, Unit.GRAM, 300L, 35L);

            log.info("Seeded {} product generals", productGeneralRepository.count());

            // Seed Product Batches - Fresh Food Stock with expiry dates
            // Thịt Gà (subSubcategoryId: 1)
            createProductBatch(100L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 6, 0), LocalDateTime.of(2026, 4, 10, 23, 59), null, 1L, ProductBatchProcessStatus.PENDING);

            // Thịt Vịt (subSubcategoryId: 2)
            createProductBatch(80L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 6, 30), LocalDateTime.of(2026, 4, 10, 23, 59), null, 2L, ProductBatchProcessStatus.PENDING);

            // Thịt Ngan (subSubcategoryId: 3)
            createProductBatch(50L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 7, 0), LocalDateTime.of(2026, 4, 10, 23, 59), null, 3L, ProductBatchProcessStatus.PENDING);

            // Thịt Chim Cút (subSubcategoryId: 4)
            createProductBatch(30L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 7, 30), LocalDateTime.of(2026, 4, 10, 23, 59), null, 4L, ProductBatchProcessStatus.PENDING);

            // Lòng Gia Cầm (subSubcategoryId: 5)
            createProductBatch(25L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 8, 0), LocalDateTime.of(2026, 4, 9, 23, 59), null, 5L, ProductBatchProcessStatus.PENDING);

            // Thịt Bò (subSubcategoryId: 6)
            createProductBatch(150L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 5, 30), LocalDateTime.of(2026, 4, 11, 23, 59), null, 6L, ProductBatchProcessStatus.PENDING);

            // Thịt Heo (subSubcategoryId: 7)
            createProductBatch(200L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 5, 0), LocalDateTime.of(2026, 4, 11, 23, 59), null, 7L, ProductBatchProcessStatus.PENDING);

            // Thịt Dê (subSubcategoryId: 8)
            createProductBatch(60L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 6, 0), LocalDateTime.of(2026, 4, 11, 23, 59), null, 8L, ProductBatchProcessStatus.PENDING);

            // Thịt Cừu (subSubcategoryId: 9)
            createProductBatch(40L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 6, 30), LocalDateTime.of(2026, 4, 12, 23, 59), null, 9L, ProductBatchProcessStatus.PENDING);

            // Xúc Xích Tươi (subSubcategoryId: 10)
            createProductBatch(50L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 7, 0), LocalDateTime.of(2026, 4, 11, 23, 59), null, 10L, ProductBatchProcessStatus.PENDING);

            // Tôm Tươi (subSubcategoryId: 11)
            createProductBatch(80L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 4, 0), LocalDateTime.of(2026, 4, 9, 23, 59), null, 11L, ProductBatchProcessStatus.PENDING);

            // Cá Tươi (subSubcategoryId: 12)
            createProductBatch(120L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 4, 30), LocalDateTime.of(2026, 4, 10, 23, 59), null, 12L, ProductBatchProcessStatus.PENDING);

            // Mực Tươi (subSubcategoryId: 13)
            createProductBatch(60L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 4, 45), LocalDateTime.of(2026, 4, 9, 23, 59), null, 13L, ProductBatchProcessStatus.PENDING);

            // Cua Ghẹ (subSubcategoryId: 14)
            createProductBatch(70L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 5, 0), LocalDateTime.of(2026, 4, 9, 23, 59), null, 14L, ProductBatchProcessStatus.PENDING);

            // Nghêu Sò (subSubcategoryId: 15)
            createProductBatch(90L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 5, 15), LocalDateTime.of(2026, 4, 9, 23, 59), null, 15L, ProductBatchProcessStatus.PENDING);

            // Rau Muống (subSubcategoryId: 16)
            createProductBatch(50L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 0), LocalDateTime.of(2026, 4, 10, 23, 59), null, 16L, ProductBatchProcessStatus.PENDING);

            // Cải Xanh (subSubcategoryId: 17)
            createProductBatch(60L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 15), LocalDateTime.of(2026, 4, 10, 23, 59), null, 17L, ProductBatchProcessStatus.PENDING);

            // Xà Lách (subSubcategoryId: 18)
            createProductBatch(40L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 30), LocalDateTime.of(2026, 4, 11, 23, 59), null, 18L, ProductBatchProcessStatus.PENDING);

            // Rau Dền (subSubcategoryId: 19)
            createProductBatch(45L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 45), LocalDateTime.of(2026, 4, 10, 23, 59), null, 19L, ProductBatchProcessStatus.PENDING);

            // Cải Thìa (subSubcategoryId: 20)
            createProductBatch(55L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 4, 0), LocalDateTime.of(2026, 4, 10, 23, 59), null, 20L, ProductBatchProcessStatus.PENDING);

            // Cà Rốt (subSubcategoryId: 21)
            createProductBatch(100L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 2, 0), LocalDateTime.of(2026, 4, 15, 23, 59), null, 21L, ProductBatchProcessStatus.PENDING);

            // Khoai Tây (subSubcategoryId: 22)
            createProductBatch(150L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 2, 30), LocalDateTime.of(2026, 4, 18, 23, 59), null, 22L, ProductBatchProcessStatus.PENDING);

            // Củ Cải (subSubcategoryId: 23)
            createProductBatch(80L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 2, 45), LocalDateTime.of(2026, 4, 15, 23, 59), null, 23L, ProductBatchProcessStatus.PENDING);

            // Bắp (subSubcategoryId: 24)
            createProductBatch(120L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 0), LocalDateTime.of(2026, 4, 12, 23, 59), null, 24L, ProductBatchProcessStatus.PENDING);

            // Su Su (subSubcategoryId: 25)
            createProductBatch(70L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 15), LocalDateTime.of(2026, 4, 14, 23, 59), null, 25L, ProductBatchProcessStatus.PENDING);

            // Xoài (subSubcategoryId: 26)
            createProductBatch(90L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 1, 0), LocalDateTime.of(2026, 4, 13, 23, 59), null, 26L, ProductBatchProcessStatus.PENDING);

            // Chuối (subSubcategoryId: 27)
            createProductBatch(100L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 1, 30), LocalDateTime.of(2026, 4, 12, 23, 59), null, 27L, ProductBatchProcessStatus.PENDING);

            // Dưa Hấu (subSubcategoryId: 28)
            createProductBatch(200L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 2, 0), LocalDateTime.of(2026, 4, 15, 23, 59), null, 28L, ProductBatchProcessStatus.PENDING);

            // Ổi (subSubcategoryId: 29)
            createProductBatch(80L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 2, 30), LocalDateTime.of(2026, 4, 14, 23, 59), null, 29L, ProductBatchProcessStatus.PENDING);

            // Thanh Long (subSubcategoryId: 30)
            createProductBatch(110L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 3, 0), LocalDateTime.of(2026, 4, 16, 23, 59), null, 30L, ProductBatchProcessStatus.PENDING);

            // Hành Lá (subSubcategoryId: 31)
            createProductBatch(30L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 4, 0), LocalDateTime.of(2026, 4, 12, 23, 59), null, 31L, ProductBatchProcessStatus.PENDING);

            // Tỏi (subSubcategoryId: 32)
            createProductBatch(40L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 4, 30), LocalDateTime.of(2026, 5, 8, 23, 59), null, 32L, ProductBatchProcessStatus.PENDING);

            // Gừng (subSubcategoryId: 33)
            createProductBatch(35L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 5, 0), LocalDateTime.of(2026, 5, 8, 23, 59), null, 33L, ProductBatchProcessStatus.PENDING);

            // Ớt (subSubcategoryId: 34)
            createProductBatch(25L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 5, 30), LocalDateTime.of(2026, 4, 15, 23, 59), null, 34L, ProductBatchProcessStatus.PENDING);

            // Sả (subSubcategoryId: 35)
            createProductBatch(20L, Unit.KILOGRAM, LocalDateTime.of(2026, 4, 8, 6, 0), LocalDateTime.of(2026, 4, 18, 23, 59), null, 35L, ProductBatchProcessStatus.PENDING);

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

    private SubSubcategory createSubSubcategory(Long id, String name, String description, String iconUrl, Long subcategoryId) {
        SubSubcategory subSubcategory = new SubSubcategory();
        subSubcategory.setSubSubcategoryId(id);
        subSubcategory.setName(name);
        subSubcategory.setDescription(description);
        subSubcategory.setIconUrl(iconUrl);
        subSubcategory.setSubcategoryId(subcategoryId);
        return subSubcategoryRepository.save(subSubcategory);
    }

    private ProductGeneral createProductGeneral(Long prodGenId, String name, String description, String[] tags,
                                                Unit unit, Long unitQuantity, Long subSubcategoryId) {
        ProductGeneral productGeneral = new ProductGeneral();
        productGeneral.setProdGenId(prodGenId);
        productGeneral.setName(name);
        productGeneral.setDescription(description);
        productGeneral.setSubSubcategoryId(subSubcategoryId);
        productGeneral.setUnit(unit);
        productGeneral.setUnitQuantity(unitQuantity);
        return productGeneralRepository.save(productGeneral);
    }

    private ProductBatch createProductBatch(Long quantity, Unit unit,
                                           LocalDateTime receivedAt, LocalDateTime expiredAt,
                                           Long providerId, Long subSubcategoryId,
                                           ProductBatchProcessStatus processStatus) {
        ProductBatch batch = new ProductBatch(quantity, unit, "", receivedAt, expiredAt, providerId, subSubcategoryId);
        batch.setProcessStatus(processStatus);
        return productBatchRepository.save(batch);
    }
}

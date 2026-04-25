package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.messaging.productgeneral.ProductGeneralCreatedEvent;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.exception.ProductGeneralNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductGeneralRepository;
import edu.hcmut.datn.productstorage.service.ProductGeneralService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ProductGeneralServiceImpl implements ProductGeneralService {

    private final ProductGeneralRepository productGeneralRepository;
    private final ProductBatchService productBatchService;

    private static final Set<Unit> MASS_UNITS = Set.of(Unit.KILOGRAM, Unit.GRAM);
    private static final Set<Unit> VOLUME_UNITS = Set.of(Unit.LITER, Unit.MILLILITER);
    
    @Override
    public ProductGeneral create(ProductGeneral productGen) {
        return productGeneralRepository.save(productGen);
    }
    
    @Override
    public ProductGeneral read(Long productGenId) {
        return productGeneralRepository.findById(productGenId)
                .orElseThrow(() -> new ProductGeneralNotFoundException("Product general not found"));
    }
    
    @Override
    public List<ProductGeneral> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return productGeneralRepository.findAll(pageable).toList();
    }
    
    @Override
    public ProductGeneral update(Long productGenId, ProductGeneral productGen) {
        ProductGeneral curProductGeneral = read(productGenId);
        
        if (productGen.getName() != null) {
            curProductGeneral.setName(productGen.getName());
        }
        if (productGen.getProdGenId() != null) {
            curProductGeneral.setProdGenId(productGen.getProdGenId());
        }
        if (productGen.getImgUrl() != null) {
            curProductGeneral.setImgUrl(productGen.getImgUrl());
        }
        if (productGen.getDescription() != null) {
            curProductGeneral.setDescription(productGen.getDescription());
        }
        if (productGen.getSubSubcategoryId() != null) {
            curProductGeneral.setSubSubcategoryId(productGen.getSubSubcategoryId());
        }
        if (productGen.getUnit() != null) {
            curProductGeneral.setUnit(productGen.getUnit());
        }
        if (productGen.getUnitQuantity() != null) {
            curProductGeneral.setUnitQuantity(productGen.getUnitQuantity());
        }
        
        return productGeneralRepository.save(curProductGeneral);
    }
    
    @Override
    public void delete(Long productGenId) {
        ProductGeneral productGeneral = read(productGenId);
        productGeneralRepository.delete(productGeneral);
    }
    
    @Override
    public ProductGeneral create(ProductGeneralCreatedEvent event) {
        return productGeneralRepository.save(event.toProductGeneralEntity());
    }

    @Override
    public List<ProductGeneral> getSuitableForBatch(Long batchId) {
        // Get the batch
        ProductBatch batch = productBatchService.read(batchId);

        // Find all product generals with matching subSubcategoryId
        List<ProductGeneral> matchingCategory = productGeneralRepository.findBySubSubcategoryId(batch.getSubSubcategoryId());

        // Filter by unit compatibility
        return matchingCategory.stream()
                .filter(pg -> areUnitsCompatible(batch.getUnit(), pg.getUnit()))
                .collect(Collectors.toList());
    }

    private boolean areUnitsCompatible(Unit unit1, Unit unit2) {
        boolean bothMass = MASS_UNITS.contains(unit1) && MASS_UNITS.contains(unit2);
        boolean bothVolume = VOLUME_UNITS.contains(unit1) && VOLUME_UNITS.contains(unit2);
        return bothMass || bothVolume;
    }
}
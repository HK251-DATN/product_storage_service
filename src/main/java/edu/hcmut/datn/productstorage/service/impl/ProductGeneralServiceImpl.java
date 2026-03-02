package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

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

    @Override
    public ProductGeneral create(ProductGeneral productGen) {
        return productGeneralRepository.save(productGen);
    }

    @Override
    public ProductGeneral read(Long productGenId) {
        return productGeneralRepository.findById(productGenId).orElseThrow(() -> new ProductGeneralNotFoundException("Product general not found"));
    }

    @Override
    public List<ProductGeneral> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);

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

        return productGeneralRepository.save(curProductGeneral);

    }

    @Override
    public void delete(Long productGenId) {
        ProductGeneral productGeneral = read(productGenId);

        productGeneralRepository.delete(productGeneral);
    }

}

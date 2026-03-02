package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.repository.ProductGeneralRepository;
import edu.hcmut.datn.productstorage.service.ProductGeneralService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductGeneralServiceImpl implements ProductGeneralService {

    private final ProductGeneralRepository productGeneralRepository;

    @Override
    public ProductGeneral create(ProductGeneral fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public ProductGeneral read(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public List<ProductGeneral> readAll(Integer pageNum, Integer pageSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAll'");
    }

    @Override
    public ProductGeneral update(Long fridgeId, ProductGeneral fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}

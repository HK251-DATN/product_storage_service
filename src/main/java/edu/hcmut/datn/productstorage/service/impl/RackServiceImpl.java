package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.Rack;
import edu.hcmut.datn.productstorage.repository.RackRepository;
import edu.hcmut.datn.productstorage.service.RackService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RackServiceImpl implements RackService {

    private final RackRepository rackRepository;

    @Override
    public Rack create(Rack fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public Rack read(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public List<Rack> readAll(Integer pageNum, Integer pageSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAll'");
    }

    @Override
    public Rack update(Long fridgeId, Rack fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}

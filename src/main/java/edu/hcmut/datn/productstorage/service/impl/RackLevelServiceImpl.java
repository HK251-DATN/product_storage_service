package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.RackLevel;
import edu.hcmut.datn.productstorage.repository.RackLevelRepository;
import edu.hcmut.datn.productstorage.service.RackLevelService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RackLevelServiceImpl implements RackLevelService {

    private final RackLevelRepository rackLevelRepository;

    @Override
    public RackLevel create(RackLevel fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public RackLevel read(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public List<RackLevel> readAll(Integer pageNum, Integer pageSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAll'");
    }

    @Override
    public RackLevel update(Long fridgeId, RackLevel fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}

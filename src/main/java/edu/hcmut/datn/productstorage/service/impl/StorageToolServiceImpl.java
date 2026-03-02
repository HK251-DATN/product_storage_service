package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.StorageTool;
import edu.hcmut.datn.productstorage.repository.StorageToolRepository;
import edu.hcmut.datn.productstorage.service.StorageToolService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StorageToolServiceImpl implements StorageToolService {

    private final StorageToolRepository storageToolRepository;

    @Override
    public StorageTool create(StorageTool fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public StorageTool read(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public List<StorageTool> readAll(Integer pageNum, Integer pageSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAll'");
    }

    @Override
    public StorageTool update(Long fridgeId, StorageTool fridge) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long fridgeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}

package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.StorageTool;

public interface StorageToolService {

    StorageTool create(StorageTool fridge);

    StorageTool read(Long fridgeId);

//    List<StorageTool> readAll(Integer pageNum, Integer pageSize);
    
    public List<StorageTool> readAll(Long warehouseId, Integer pageNum, Integer pageSize);
    
    StorageTool update(Long fridgeId, StorageTool fridge);

    void delete(Long fridgeId);    
}

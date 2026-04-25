package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.Warehouse;

public interface WarehouseService {

    Warehouse create(Warehouse fridge);

    Warehouse read(Long fridgeId);

    List<Warehouse> readAll(Integer pageNum, Integer pageSize);

    Warehouse update(Long fridgeId, Warehouse fridge);

    void delete(Long fridgeId);    
}

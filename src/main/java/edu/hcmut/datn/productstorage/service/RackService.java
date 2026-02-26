package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.Rack;

public interface RackService {

    Rack create(Rack fridge);

    Rack read(Long fridgeId);

    List<Rack> readAll(Integer pageNum, Integer pageSize);

    Rack update(Long fridgeId, Rack fridge);

    void delete(Long fridgeId);    
}

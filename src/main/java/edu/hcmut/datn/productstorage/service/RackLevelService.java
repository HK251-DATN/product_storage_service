package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.RackLevel;

public interface RackLevelService {

    RackLevel create(RackLevel fridge);

    RackLevel read(Long fridgeId);

    List<RackLevel> readAll(Integer pageNum, Integer pageSize);

    RackLevel update(Long fridgeId, RackLevel fridge);

    void delete(Long fridgeId);    
}

package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.Fridge;

public interface FridgeService {

    Fridge create(Fridge fridge);

    Fridge read(Long fridgeId);

    List<Fridge> readAll(Integer pageNum, Integer pageSize);

    Fridge update(Long fridgeId, Fridge fridge);

    void delete(Long fridgeId);    
}

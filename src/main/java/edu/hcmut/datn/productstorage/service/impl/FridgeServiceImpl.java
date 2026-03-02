package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.Fridge;
import edu.hcmut.datn.productstorage.repository.FridgeRepository;
import edu.hcmut.datn.productstorage.service.FridgeService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FridgeServiceImpl implements FridgeService {

    private final FridgeRepository fridgeRepository;

    @Override
    public Fridge create(Fridge fridge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Fridge read(Long fridgeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Fridge> readAll(Integer pageNum, Integer pageSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Fridge update(Long fridgeId, Fridge fridge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Long fridgeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

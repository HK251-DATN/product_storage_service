package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.StorageTool;
import edu.hcmut.datn.productstorage.dao.Warehouse;
import edu.hcmut.datn.productstorage.service.StorageToolService;
import edu.hcmut.datn.productstorage.service.WarehouseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.Fridge;
import edu.hcmut.datn.productstorage.exception.FridgeAlreadyExistsException;
import edu.hcmut.datn.productstorage.exception.FridgeNotFoundException;
import edu.hcmut.datn.productstorage.repository.FridgeRepository;
import edu.hcmut.datn.productstorage.service.FridgeService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class FridgeServiceImpl implements FridgeService {

    private final FridgeRepository fridgeRepository;
    
    private final WarehouseService warehouseService;
    
    private final StorageToolService storageToolService;

    @Override
    public Fridge create(Fridge fridge) {
        if (fridgeRepository.existsByStorageToolId(fridge.getStorageToolId())) {
            throw new FridgeAlreadyExistsException("This storage tool id has been associated with another tool");
        }
        
        StorageTool storageTool = storageToolService.read(fridge.getStorageToolId());
        
        Warehouse warehouse = warehouseService.read(storageTool.getWarehouseId());
        
        warehouse.setNumOfRack(warehouse.getNumOfFridge() + 1);

        return fridgeRepository.save(fridge);
    }

    @Override
    public Fridge read(Long fridgeId) {
        return fridgeRepository.findById(fridgeId).orElseThrow(() -> new FridgeNotFoundException("Fridge not found"));
    }

    @Override
    public List<Fridge> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return fridgeRepository.findAll(pageable).toList();
    }

    @Override
    public Fridge update(Long fridgeId, Fridge fridge) {
        Fridge curFridge = read(fridgeId);

        if (fridge.getCurTemp() != null) {
            curFridge.setCurTemp(fridge.getCurTemp());
        }
        if (fridge.getMinTemp() != null) {
            curFridge.setMinTemp(fridge.getMinTemp());
        }
        if (fridge.getMaxTemp() != null) {
            curFridge.setMaxTemp(fridge.getMaxTemp());
        }
        if (fridge.getStorageToolId() != null) {
            curFridge.setStorageToolId(fridge.getStorageToolId());
        }

        return fridgeRepository.save(curFridge);
    }

    @Override
    public void delete(Long fridgeId) {
        Fridge curFridge = read(fridgeId);

        fridgeRepository.delete(curFridge);
    }
}

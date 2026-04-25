package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.StorageTool;
import edu.hcmut.datn.productstorage.dao.Warehouse;
import edu.hcmut.datn.productstorage.service.StorageToolService;
import edu.hcmut.datn.productstorage.service.WarehouseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.Rack;
import edu.hcmut.datn.productstorage.exception.RackAlreadyExistsException;
import edu.hcmut.datn.productstorage.exception.RackNotFoundException;
import edu.hcmut.datn.productstorage.repository.RackRepository;
import edu.hcmut.datn.productstorage.service.RackService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class RackServiceImpl implements RackService {

    private final RackRepository rackRepository;
    
    private final WarehouseService warehouseService;
    
    private final StorageToolService storageToolService;

    @Override
    public Rack create(Rack rack) {
        if (rackRepository.existsByStorageToolId(rack.getStorageToolId())) {
            throw new RackAlreadyExistsException("This storage tool id has been associated with another tool");
        }
        
        StorageTool storageTool = storageToolService.read(rack.getStorageToolId());
        
        Warehouse warehouse = warehouseService.read(storageTool.getWarehouseId());
        
        warehouse.setNumOfRack(warehouse.getNumOfRack() + 1);

        return rackRepository.save(rack);
    }

    @Override
    public Rack read(Long rackId) {
        return rackRepository.findById(rackId).orElseThrow(() -> new RackNotFoundException("Rack not found"));
    }

    @Override
    public List<Rack> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return rackRepository.findAll(pageable).toList();
    }

    @Override
    public Rack update(Long rackId, Rack rack) {
        Rack curRack = read(rackId);

        if (rack.getNumOfLevel() != null) {
            curRack.setNumOfLevel(rack.getNumOfLevel());
        }

        if (rack.getStorageToolId() != null) {
            curRack.setStorageToolId(rack.getStorageToolId());
        }

        return rackRepository.save(curRack);
    }

    @Override
    public void delete(Long rackId) {
        Rack rack = read(rackId);

        rackRepository.delete(rack);
    }

}

package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.Warehouse;
import edu.hcmut.datn.productstorage.exception.WarehouseNotFoundException;
import edu.hcmut.datn.productstorage.repository.WarehouseRepository;
import edu.hcmut.datn.productstorage.service.WarehouseService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    public Warehouse create(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    @Override
    public Warehouse read(Long warehouseId) {
        return warehouseRepository.findById(warehouseId).orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found"));
    }

    @Override
    public List<Warehouse> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return warehouseRepository.findAll(pageable).toList();
    }

    @Override
    public Warehouse update(Long warehouseId, Warehouse warehouse) {
        Warehouse curWarehouse = read(warehouseId);

        if (warehouse.getAddress() != null) {
            curWarehouse.setAddress(warehouse.getAddress());
        }

        if (warehouse.getUsagePercentage() != null) {
            curWarehouse.setUsagePercentage(warehouse.getUsagePercentage());
        }

        if (warehouse.getNumOfFridge() != null) {
            curWarehouse.setNumOfFridge(warehouse.getNumOfFridge());
        }

        if (warehouse.getNumOfRack() != null) {
            curWarehouse.setNumOfRack(warehouse.getNumOfRack());
        }

        return warehouseRepository.save(curWarehouse);
    }

    @Override
    public void delete(Long warehouseId) {
        Warehouse warehouse = read(warehouseId);

        warehouseRepository.delete(warehouse);
    }

}

package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.StorageTool;
import edu.hcmut.datn.productstorage.exception.StorageToolNotFoundException;
import edu.hcmut.datn.productstorage.repository.StorageToolRepository;
import edu.hcmut.datn.productstorage.service.StorageToolService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class StorageToolServiceImpl implements StorageToolService {

    private final StorageToolRepository storageToolRepository;

    @Override
    public StorageTool create(StorageTool storageTool) {
        return storageToolRepository.save(storageTool);
    }

    @Override
    public StorageTool read(Long storageToolId) {
        return storageToolRepository.findById(storageToolId).orElseThrow(() -> new StorageToolNotFoundException("Storage tool not found"));
    }

    @Override
    public List<StorageTool> readAll(Long warehouseId, Integer pageNum, Integer pageSize) {
        return storageToolRepository.search(warehouseId, pageNum, pageSize);
    }

    @Override
    public StorageTool update(Long storageToolId, StorageTool storageTool) {
        StorageTool curStorageTool = read(storageToolId);

        if (storageTool.getLastMaintainanceDate() != null) {
            curStorageTool.setLastMaintainanceDate(storageTool.getLastMaintainanceDate());
        }
        if (storageTool.getStatus() != null) {
            curStorageTool.setStatus(storageTool.getStatus());
        }
        if (storageTool.getUsagePercentage() != null) {
            curStorageTool.setUsagePercentage(storageTool.getUsagePercentage());
        }
        if (storageTool.getWarehouseId() != null) {
            curStorageTool.setWarehouseId(storageTool.getWarehouseId());
        }
        if (storageTool.getToolType() != null) {
            curStorageTool.setToolType(storageTool.getToolType());
        }

        return storageToolRepository.save(curStorageTool);
    }

    @Override
    public void delete(Long storageToolId) {
        StorageTool storageTool = read(storageToolId);

        storageToolRepository.delete(storageTool);
    }

}

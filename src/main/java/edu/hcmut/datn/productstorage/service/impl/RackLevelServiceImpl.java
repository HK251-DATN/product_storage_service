package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.RackLevel;
import edu.hcmut.datn.productstorage.exception.RackLevelNotFoundException;
import edu.hcmut.datn.productstorage.repository.RackLevelRepository;
import edu.hcmut.datn.productstorage.service.RackLevelService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class RackLevelServiceImpl implements RackLevelService {

    private final RackLevelRepository rackLevelRepository;

    @Override
    public RackLevel create(RackLevel rackLevel) {
        // TODO: If current rack reach max level, cannot add more level to the rack

        return rackLevelRepository.save(rackLevel);
    }

    @Override
    public RackLevel read(Long rackLevelId) {
        return rackLevelRepository.findById(rackLevelId).orElseThrow(() -> new RackLevelNotFoundException("Rack level not found"));
    }

    @Override
    public List<RackLevel> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return rackLevelRepository.findAll(pageable).toList();
    }

    @Override
    public RackLevel update(Long rackLevelId, RackLevel rackLevel) {
        RackLevel curRackLevel = read(rackLevelId);

        if (rackLevel.getUsagePercentage() != null) {
            curRackLevel.setUsagePercentage(rackLevel.getUsagePercentage());
        }
        if (rackLevel.getRackId() != null) {
            curRackLevel.setRackId(rackLevel.getRackId());
        }

        return rackLevelRepository.save(curRackLevel);
    }

    @Override
    public void delete(Long rackLevelId) {
        RackLevel rackLevel = read(rackLevelId);

        rackLevelRepository.delete(rackLevel);
    }

}

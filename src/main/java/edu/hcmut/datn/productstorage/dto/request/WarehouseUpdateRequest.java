package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.dao.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseUpdateRequest {
    private String address;
    private Long usagePercentage;
    private Long numOfFridge;
    private Long numOfRack;

    public Warehouse toEntity() {
        return new Warehouse(address, usagePercentage, numOfFridge, numOfRack);
    }
}

package edu.hcmut.datn.productstorage.dto.request;

import java.time.LocalDate;

import edu.hcmut.datn.productstorage.common.enums.StorageToolStatus;
import edu.hcmut.datn.productstorage.common.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageToolUpdateRequest {
    private LocalDate lastMaintainanceDate;
    private StorageToolStatus status;
    private Long usagePercentage;
    private Long warehouseId;
    private StorageType toolType;
}

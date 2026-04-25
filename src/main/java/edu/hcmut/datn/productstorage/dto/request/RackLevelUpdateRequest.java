package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.dao.RackLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackLevelUpdateRequest {

    private Long usagePercentage;
    private Long rackId;

    public RackLevel toEntity() {
        return new RackLevel(usagePercentage, rackId);
    }
}

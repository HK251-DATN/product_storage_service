package edu.hcmut.datn.productstorage.dto.request;

import java.time.LocalDate;

import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawProductDemandCreateRequest {

    private Long subSubcategoryId;
    private Unit unit;
    private Long unitQuantity;
    private Long unitPrice;
    private LocalDate dateNeed;
    private String note;
    private RawProductDemandStatus status;

    public RawProductDemand toEntity() {
        RawProductDemand demand = new RawProductDemand(subSubcategoryId, unit, unitQuantity, unitPrice, dateNeed, note);
        if (status != null) {
            demand.setStatus(status);
        }
        return demand;
    }
}

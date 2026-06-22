package edu.hcmut.datn.productstorage.dto.request;

import java.time.LocalDate;

import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawProductDemandUpdateRequest {

    private Long subSubcategoryId;
    private Unit unit;
    private Long unitQuantity;
    private Long unitPrice;
    private LocalDate dateNeed;
    private String note;
    private RawProductDemandStatus status;
}

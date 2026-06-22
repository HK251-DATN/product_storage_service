package edu.hcmut.datn.productstorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawProductDemandConfirmRequest {

    private Long quantity;
    private String note;
}

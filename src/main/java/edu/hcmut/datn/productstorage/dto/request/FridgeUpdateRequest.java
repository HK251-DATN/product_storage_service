package edu.hcmut.datn.productstorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeUpdateRequest {

    private Long curTemp;
    private Long minTemp;
    private Long maxTemp;
    private Long storageToolId;

}

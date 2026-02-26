package edu.hcmut.datn.productstorage.dto.request;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchUpdateRequest {

    private Long quantity;
    private Unit unit;
    private String note;
    private LocalDateTime receivedAt;
    private LocalDateTime expiredAt;
    private Long providerId;

}

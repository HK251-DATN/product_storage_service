package edu.hcmut.datn.productstorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackUpdateRequest {
    private Long numOfLevel;
    private Long storageToolId;
}

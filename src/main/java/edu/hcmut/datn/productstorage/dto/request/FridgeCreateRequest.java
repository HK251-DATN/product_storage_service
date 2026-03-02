package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.dao.Fridge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeCreateRequest {

    private Long curTemp;
    private Long minTemp;
    private Long maxTemp;
    private Long storageToolId;

    public Fridge toEntity() {
        return new Fridge(curTemp, minTemp, maxTemp, storageToolId);
    }
}

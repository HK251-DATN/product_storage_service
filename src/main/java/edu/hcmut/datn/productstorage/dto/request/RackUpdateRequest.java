package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.dao.Rack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RackUpdateRequest {
    private Long numOfLevel;
    private Long storageToolId;

    public Rack toEntity() {
        return new Rack(numOfLevel, storageToolId);
    }
}

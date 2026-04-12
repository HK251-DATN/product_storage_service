package edu.hcmut.datn.productstorage.controller;

import edu.hcmut.datn.productstorage.service.PickListService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pick-list")
@AllArgsConstructor
public class PickListController {

    private final PickListService pickListService;

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getPickList(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(pickListService.getPickList(orderId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{orderItemId}/link/{productDetailId}")
    public ResponseEntity<?> linkOrderItem(@PathVariable Long orderItemId, @PathVariable Long productDetailId) {
        try {
            pickListService.linkOrderItem(orderItemId, productDetailId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

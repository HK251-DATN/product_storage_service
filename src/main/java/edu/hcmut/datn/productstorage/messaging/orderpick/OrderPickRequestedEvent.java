package edu.hcmut.datn.productstorage.messaging.orderpick;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderPickRequestedEvent {
    private Long orderId;
    private List<OrderItemInfo> orderItems;
    private Long buyerId;

    public record OrderItemInfo(String batchDetailId, Long quantity) {}
}

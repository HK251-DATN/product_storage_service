package edu.hcmut.datn.productstorage.repository;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.repository.projector.PickListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    
    @Query(
        value = """
                WITH ProductSummary AS (
                    SELECT batch_id, name, unit, product_generals.prod_gen_id
                    FROM product_details\s
                    JOIN product_generals ON product_details.prod_gen_id = product_generals.prod_gen_id
                    GROUP BY batch_id, product_generals.prod_gen_id, name, unit
                ),
                FilteredOrders AS (
                    SELECT order_item_id, batch_detail_id, buyer_id, order_id, product_detail_id
                    FROM ORDER_ITEMS
                    WHERE order_id = :order_id
                )
                SELECT * FROM ProductSummary ps
                INNER JOIN FilteredOrders fo ON ps.batch_id = fo.batch_detail_id;
                """,
            nativeQuery = true
    )
    List<PickListItem> findPickListItemByOrderId(
            @Param("order_id") Long orderId
    );
    
    /**
     * Find order item by batch detail ID
     */
    Optional<OrderItem> findByBatchDetailId(Long batchDetailId);
    
    /**
     * Find order items by buyer ID
     */
    List<OrderItem> findByBuyerId(String buyerId);
    
    /**
     * Check if order item exists
     */
    boolean existsByOrderItemId(Long orderItemId);
}
package edu.hcmut.datn.productstorage.service.impl;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.dao.ProductDetail;
import edu.hcmut.datn.productstorage.repository.OrderItemRepository;
import edu.hcmut.datn.productstorage.repository.ProductDetailRepository;
import edu.hcmut.datn.productstorage.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    private final ProductDetailRepository productDetailRepository;
    
    @Override
    public Page<OrderItem> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return orderItemRepository.findAll(pageable);
    }
    
    @Override
    public OrderItem read(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("OrderItem not found with id: " + orderItemId));
    }
    
    @Override
    public List<OrderItem> readByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<OrderItem> readByBuyerId(String buyerId) {
        return orderItemRepository.findByBuyerId(buyerId);
    }
    
    @Override
    @Transactional
    public OrderItem assignProductDetail(Long orderItemId, Long productDetailId) {
        // Get order item
        OrderItem orderItem = read(orderItemId);
        
        // Check if already assigned
        if (orderItem.getProductDetailId() != null) {
            throw new RuntimeException("OrderItem already has a ProductDetail assigned");
        }
        
        // Verify product detail exists
        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("ProductDetail not found with id: " + productDetailId));
        
        // Assign product detail (1-to-1 relationship)
        orderItem.setProductDetailId(productDetailId);
        
        return orderItemRepository.save(orderItem);
    }
    
    @Override
    public List<OrderItem> getPendingOrderItems() {
        // Get all order items where productDetailId is null
        return orderItemRepository.findAll().stream()
                .filter(item -> item.getProductDetailId() == null)
                .collect(Collectors.toList());
    }
}
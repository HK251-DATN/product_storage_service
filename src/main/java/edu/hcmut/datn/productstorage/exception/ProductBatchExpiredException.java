package edu.hcmut.datn.productstorage.exception;

public class ProductBatchExpiredException extends RuntimeException {
    public ProductBatchExpiredException(String message) {
        super(message);
    }
}

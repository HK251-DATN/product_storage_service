package edu.hcmut.datn.productstorage.exception;

public class ProductBatchAlreadyExistsException extends RuntimeException {

    public ProductBatchAlreadyExistsException(String message) {
        super(message);
    }
}

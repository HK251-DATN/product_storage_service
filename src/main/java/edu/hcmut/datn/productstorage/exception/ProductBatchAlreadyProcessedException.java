package edu.hcmut.datn.productstorage.exception;

public class ProductBatchAlreadyProcessedException extends RuntimeException {
    public ProductBatchAlreadyProcessedException(String message) {
        super(message);
    }
}

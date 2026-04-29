package edu.hcmut.datn.productstorage.exception;

public class ProductSubBatchAlreadyProcessedException extends RuntimeException {
    public ProductSubBatchAlreadyProcessedException(String message) {
        super(message);
    }
}

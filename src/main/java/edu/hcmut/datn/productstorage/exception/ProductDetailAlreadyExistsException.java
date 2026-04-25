package edu.hcmut.datn.productstorage.exception;

public class ProductDetailAlreadyExistsException extends RuntimeException {

    public ProductDetailAlreadyExistsException(String message) {
        super(message);
    }
}

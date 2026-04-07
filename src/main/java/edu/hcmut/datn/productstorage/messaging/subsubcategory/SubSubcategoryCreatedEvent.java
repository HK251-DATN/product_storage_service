package edu.hcmut.datn.productstorage.messaging.subsubcategory;

public record SubSubcategoryCreatedEvent(
        Long subSubcategoryId,
        String name,
        String description,
        String iconUrl,
        Long subcategoryId
) {}
package edu.hcmut.datn.productstorage.messaging.category;

import edu.hcmut.datn.productstorage.dao.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CategoryCreatedEvent {

    @Getter
    private Long categoryId;


    @Getter
    private String name;


    @Getter
    private String description;

    public Category toCategoryEntity() {
        Category newCategory = new Category();

        newCategory.setCategoryId(categoryId);
        newCategory.setName(name);
        newCategory.setDescription(description);

        return newCategory;
    }
}

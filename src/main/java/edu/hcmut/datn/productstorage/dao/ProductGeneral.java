package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="product_generals")
@NoArgsConstructor
public class ProductGeneral {
    @Id
    @Column(name="prod_gen_id")
    @Getter
    @Setter
    private Long prodGenId;
    
    @Column(name="name")
    @Getter
    @Setter
    private String name;
    
    @Column(name = "img")
    @Getter
    @Setter
    private String imgUrl;
    
    @Column(name = "description")
    @Getter
    @Setter
    private String description;
    
    @Column(name = "subsubcategory_id")
    @Getter
    @Setter
    private Long subSubcategoryId;
    
    @Column(name = "unit")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Unit unit;
    
    @Column(name = "unit_quantity")
    @Getter
    @Setter
    private Long unitQuantity;
    
    @Column(name="created_at")
    private LocalDateTime createdAt;
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    
    public ProductGeneral(Long prodGenId, String name) {
        this.prodGenId = prodGenId;
        this.name = name;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
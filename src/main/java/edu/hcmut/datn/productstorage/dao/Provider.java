package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "providers")
@NoArgsConstructor
public class Provider {

    @Id
    @Column(name = "provider_id")
    @Getter
    @Setter
    private Long providerId;

    @Column(name = "verification_method")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private ProviderVerificationType verificationMethod;

    @Column(name = "created_at")
    @Getter
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    public Provider(Long providerId, ProviderVerificationType verificationMethod) {
        this.providerId = providerId;
        this.verificationMethod = verificationMethod;
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

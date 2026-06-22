package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.CertificateType;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.VerificationStatus;
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

    @Column(name = "verification_status")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "certificate_type")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private CertificateType certificateType;

    @Column(name = "f_name")
    @Setter
    @Getter
    private String fName;

    @Column(name = "l_name")
    @Setter
    @Getter
    private String lName;

    @Column(name = "avt_url")
    @Setter
    @Getter
    private String avtUrl;

    @Column(name = "logo_url")
    @Setter
    @Getter
    private String logoUrl;

    @Column(name = "created_at")
    @Getter
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    public Provider(Long providerId, ProviderVerificationType verificationMethod) {
        this.providerId = providerId;
        this.verificationMethod = verificationMethod;
        this.verificationStatus = VerificationStatus.UNVERIFIED;
    }

    public Provider(Long providerId, ProviderVerificationType verificationMethod,
                   VerificationStatus verificationStatus, CertificateType certificateType) {
        this.providerId = providerId;
        this.verificationMethod = verificationMethod;
        this.verificationStatus = verificationStatus;
        this.certificateType = certificateType;
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

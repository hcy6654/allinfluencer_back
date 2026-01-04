package com.allinfluencer.backend.signup.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "advertiser_companies")
public class AdvertiserCompanyEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "userId", nullable = false, unique = true)
    private String userId;

    @Column(name = "companyName", nullable = false)
    private String companyName;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "businessRegistrationNumber")
    private String businessRegistrationNumber;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    protected AdvertiserCompanyEntity() {}

    public AdvertiserCompanyEntity(String id, String userId, String companyName, String industry, String businessRegistrationNumber) {
        this.id = id;
        this.userId = userId;
        this.companyName = companyName;
        this.industry = industry;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getCompanyName() { return companyName; }
    public String getIndustry() { return industry; }
}


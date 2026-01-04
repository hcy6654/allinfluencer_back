package com.allinfluencer.backend.auth.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_identities")
public class UserIdentityEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "provider", nullable = false)
    private String provider; // GOOGLE|KAKAO|NAVER

    @Column(name = "providerUserId", nullable = false)
    private String providerUserId;

    @Column(name = "email")
    private String email;

    @Column(name = "linkedAt", nullable = false)
    private LocalDateTime linkedAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    protected UserIdentityEntity() {}

    public UserIdentityEntity(String id, String userId, String provider, String providerUserId, String email) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (linkedAt == null) linkedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }
    public String getEmail() { return email; }
    public LocalDateTime getLinkedAt() { return linkedAt; }
}


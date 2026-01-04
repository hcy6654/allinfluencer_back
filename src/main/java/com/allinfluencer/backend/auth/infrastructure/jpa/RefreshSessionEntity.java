package com.allinfluencer.backend.auth.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_sessions")
public class RefreshSessionEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    @Column(name = "uaHash")
    private String uaHash;

    @Column(name = "ipHash")
    private String ipHash;

    @Column(name = "expiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    protected RefreshSessionEntity() {}

    public RefreshSessionEntity(String id, String userId, String jti, String uaHash, String ipHash, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.jti = jti;
        this.uaHash = uaHash;
        this.ipHash = ipHash;
        this.expiresAt = expiresAt;
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

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getJti() { return jti; }
    public String getUaHash() { return uaHash; }
    public String getIpHash() { return ipHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}


package com.allinfluencer.backend.signup.infrastructure.jpa;

import com.allinfluencer.backend.signup.domain.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "influencer_verifications")
public class InfluencerVerificationEntity {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VerificationStatus status;

    @Column(name = "method")
    private String method;

    @Column(name = "submittedAt", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "verifiedAt")
    private LocalDateTime verifiedAt;

    @Column(name = "meta", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metaJson; // 단순 저장 (jsonb). 필요 시 converter 추가

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    protected InfluencerVerificationEntity() {}

    public InfluencerVerificationEntity(String id, String userId, VerificationStatus status, String method, LocalDateTime submittedAt, String metaJson) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.method = method;
        this.submittedAt = submittedAt;
        this.metaJson = metaJson;
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

    public VerificationStatus getStatus() { return status; }
    public String getUserId() { return userId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public void markVerified(LocalDateTime now) {
        this.status = VerificationStatus.VERIFIED;
        this.verifiedAt = now;
    }

    public void markRejected(LocalDateTime now) {
        this.status = VerificationStatus.REJECTED;
        this.verifiedAt = now;
    }
}


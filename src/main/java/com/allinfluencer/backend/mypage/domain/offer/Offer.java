package com.allinfluencer.backend.mypage.domain.offer;

import com.allinfluencer.backend.mypage.domain.DomainException;

import java.time.LocalDateTime;

/**
 * Offer Aggregate (간단 버전)
 * - 상태 전이를 이 객체가 보장한다.
 */
public final class Offer {
    private final String id;
    private final String jobPostId;
    private final String senderId;
    private final String receiverId;
    private OfferStatus status;
    private LocalDateTime updatedAt;

    private Offer(
            String id,
            String jobPostId,
            String senderId,
            String receiverId,
            OfferStatus status,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.jobPostId = jobPostId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static Offer rehydrate(
            String id,
            String jobPostId,
            String senderId,
            String receiverId,
            OfferStatus status,
            LocalDateTime updatedAt
    ) {
        return new Offer(id, jobPostId, senderId, receiverId, status, updatedAt);
    }

    /**
     * 광고주(보낸 사람)가 만료 처리할 수 있다(정책은 추후 변경 가능).
     */
    public void expireBySender(String actorUserId, LocalDateTime now) {
        if (!senderId.equals(actorUserId)) {
            throw new DomainException("FORBIDDEN", "오퍼 만료 권한이 없습니다.");
        }
        if (status != OfferStatus.PENDING) {
            throw new DomainException("INVALID_STATE", "PENDING 상태의 오퍼만 만료 처리할 수 있습니다.");
        }
        this.status = OfferStatus.EXPIRED;
        this.updatedAt = now;
    }

    public String id() { return id; }
    public String jobPostId() { return jobPostId; }
    public String senderId() { return senderId; }
    public String receiverId() { return receiverId; }
    public OfferStatus status() { return status; }
    public LocalDateTime updatedAt() { return updatedAt; }
}

